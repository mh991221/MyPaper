package com.sp.fc.user.service;


import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    public User save(User user) throws DataIntegrityViolationException {
        if(user.getUserId() == null){
            user.setCreated(LocalDateTime.now());
        }
        user.setUpdated(LocalDateTime.now());
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public Page<User> listUser(int pageNum, int size){
        return userRepository.findAll(PageRequest.of(pageNum-1,size));
    }

    public Map<Long, User> getUsers(List<Long> userList){
        return StreamSupport.stream(userRepository.findAllById(userList).spliterator(), false)
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    public void addAuthority(Long id, String authority){
        userRepository.findById(id).ifPresent(user ->{
            Authority newRole = new Authority(user.getUserId(), authority);

            if(user.getAuthorities() == null){
                HashSet<Authority> authorities = new HashSet<>();

                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }else{
                HashSet<Authority> authorities = new HashSet<>();
                authorities.addAll(user.getAuthorities());

                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }
        });
    }

    public void removeAuthority(Long id, String authority){
        userRepository.findById(id).ifPresent(user -> {
            if(user.getAuthorities() == null) return;
            Authority targetRole = new Authority(user.getUserId(),authority);
            if(user.getAuthorities().contains(targetRole)){
                user.setAuthorities(
                        user.getAuthorities().stream().filter(a -> !a.equals(targetRole))
                                .collect(Collectors.toSet())
                );
                save(user);
            }
        });
    }

    public void updateUsername(Long id, String name){
        userRepository.updateUsername(id, name, LocalDateTime.now());
    }

    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public List<User> findTeacherList(){
        return userRepository.findAllByAuthoritiesIn(Authority.ROLE_TEACHER);
    }

    public List<User> findStudentList(){
        return userRepository.findAllByAuthoritiesIn(Authority.ROLE_STUDENT);
    }

    public List<User> findTeacherStudentList(Long teacherId){
        return userRepository.findTeacherStudentList(teacherId);
    }

    public Long findTeacherStudentCount(Long teacherId){
        return userRepository.countTeacherStudentList(teacherId);
    }

    public List<User> findBySchoolStudentList(Long schoolId){
        return userRepository.findAllBySchool(schoolId,Authority.ROLE_STUDENT);
    }

    public List<User> findBySchoolTeacherList(Long schoolId){
        return userRepository.findAllBySchool(schoolId,Authority.ROLE_TEACHER);
    }

    public void updateUserSchoolTeacher(Long userId, Long schoolId, Long teacherId){
        userRepository.findById(userId).ifPresent(user -> {
            if(user.getSchool().getSchoolId() != schoolId){
                schoolRepository.findById(schoolId).ifPresent(user::setSchool);
            }
            if(user.getTeacher().getUserId() != teacherId){
                userRepository.findById(teacherId).ifPresent(user::setTeacher);
            }
            if(user.getSchool().getSchoolId() != user.getTeacher().getSchool().getSchoolId()){
                throw new IllegalArgumentException("해당 학교의 선생님이 아닙니다.");
            }
            save(user);
        });
    }

    public Long countTeacher(){
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_TEACHER);
    }

    public Long countStudent(){
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_STUDENT);
    }

    public Long countTeacher(Long schoolId){
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_TEACHER, schoolId);
    }

    public Long countStudent(Long schoolId){
        return userRepository.countAllByAuthoritiesIn(Authority.ROLE_STUDENT, schoolId);
    }

    public Page<User> listStudents(int pageNum, int size){
        return userRepository.findAllByAuthoritiesIn(Authority.ROLE_STUDENT, PageRequest.of(pageNum-1,size));
    }
    public Page<User> listTeachers(int pageNum, int size){
        return userRepository.findAllByAuthoritiesIn(Authority.ROLE_TEACHER, PageRequest.of(pageNum-1,size));
    }

}
