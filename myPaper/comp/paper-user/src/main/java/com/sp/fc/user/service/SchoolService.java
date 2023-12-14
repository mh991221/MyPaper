package com.sp.fc.user.service;


import com.sp.fc.user.domain.School;
import com.sp.fc.user.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public School save(School school){
        if(school.getSchoolId() == null){
            school.setCreated(LocalDateTime.now());
        }
        school.setUpdated(LocalDateTime.now());
        return schoolRepository.save(school);
    }

    public Optional<School> findSchool(Long schoolId){
        return schoolRepository.findById(schoolId);
    }

    public Page<School> list(int pageNum, int size){
        return schoolRepository.findAllByOrderByCreatedDesc(PageRequest.of(pageNum-1, size));
    }

    public List<School> getSchoolList(String city){
        return schoolRepository.findAllByCity(city);
    }

    public Optional<School> updateName(Long schoolId, String name){
        return schoolRepository.findById(schoolId).map(school -> {
            if(school.getName() != name){
                school.setName(name);
            }
            school.setUpdated(LocalDateTime.now());
            return save(school);
        });
    }

    public List<String> cities(){
        return schoolRepository.getCities();
    }

    public long count(){
        return schoolRepository.count();
    }
}
