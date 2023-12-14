package com.sp.fc.user.service;


import com.sp.fc.user.domain.School;
import com.sp.fc.user.repository.SchoolRepository;
import com.sp.fc.user.service.helper.SchoolTestHelper;
import com.sp.fc.user.service.helper.WithUserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class SchoolTest extends WithUserTest {

//    this.school = this.schoolTestHelper.createSchool("테스트 학교", "서울");

    @BeforeEach
    void before(){prepareUserServices();}


    @DisplayName("1. 학교를 생성한다.")
    @Test
    void test_1(){
        List<School> list = schoolRepository.findAll();

        assertEquals(1, list.size());
        SchoolTestHelper.assertSchool(list.get(0), "테스트 학교", "서울");
    }

    @DisplayName("2. city list 받아오기")
    @Test
    void test_2(){
        List<String> list = schoolService.cities();

        assertEquals(1, list.size());
        schoolTestHelper.createSchool("테스트 학교 2","부산");

        list = schoolService.cities();

        assertEquals(2, list.size());
    }

    @DisplayName("3. 학교 이름 update")
    @Test
    void test_3(){
        School school2 =  schoolService.updateName(school.getSchoolId(), "테스트 학교2").get();

        SchoolTestHelper.assertSchool(school2,"테스트 학교2","서울");
    }

    @DisplayName("4. 지역으로 학교 가져오기")
    @Test
    void test_4(){
        List<School> list = schoolService.getSchoolList("서울");

        assertEquals(1, list.size());

        schoolTestHelper.createSchool("테스트 학교 2","서울");

        list = schoolService.getSchoolList("서울");

        assertEquals(2,list.size());
    }

}
