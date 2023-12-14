package com.sp.fc.user.service;

import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestHelper;
import com.sp.fc.user.service.helper.WithUserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class StudentTest extends WithUserTest {

    User teacher;
    User student;

    @BeforeEach
    void before(){
        prepareUserServices();
        teacher = userTestHelper.createTeacher(school,"teacher");
        student = userTestHelper.createStudent(school,teacher,"student","1");
    }

    @DisplayName("1. 학생 등록")
    @Test
    void test_1(){
        List<User> list = userService.findStudentList();

        assertEquals(1, list.size());

        userTestHelper.createStudent(school,teacher,"student1","1");

        list = userService.findStudentList();

        assertEquals(2, list.size());
    }

    @DisplayName("2. 선생님의 학생으로 조회된다.")
    @Test
    void test_2(){
        userTestHelper.createStudent(school,teacher,"student1","1");

        List<User> list = userService.findTeacherStudentList(teacher.getUserId());
        assertEquals(2,list.size());
        UserTestHelper.assertStudent(school,teacher,list.get(0),"student","1");
    }

    @DisplayName("3. 학교로 학생을 조회한다. ")
    @Test
    void test_3(){
        userTestHelper.createStudent(school,teacher,"student1","1");

        List<User> list = userService.findBySchoolStudentList(school.getSchoolId());
        assertEquals(2,list.size());
        UserTestHelper.assertStudent(school,teacher,list.get(0),"student","1");
    }

}
