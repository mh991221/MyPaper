package com.sp.fc.user.service;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.service.helper.UserTestHelper;
import com.sp.fc.user.service.helper.WithUserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 선생님을 등록한다.
// 선생님으로 등록한 학생 리스트를 조회한다.
// 선생님 리스트를 조회 한다.
// 학교로 선생님이 조회된다.
@DataJpaTest
public class TeacherTest extends WithUserTest {

    User teacher;

    @BeforeEach
    void before(){
        prepareUserServices();
        this.teacher = userTestHelper.createTeacher(school, "teacher");
    }

    @DisplayName("1. 선생님 생성")
    @Test
    void test_1(){
        List<User>  list = userService.findTeacherList();

        assertEquals(1,list.size());

        UserTestHelper.assertTeacher(school,list.get(0),"teacher");
    }

    @DisplayName("2. 학생 찾기")
    @Test
    void test_2(){
        userTestHelper.createStudent(school, teacher,"user1","1");
        userTestHelper.createStudent(school, teacher,"user2","1");
        userTestHelper.createStudent(school, teacher,"user3","1");

        List<User> list = userService.findTeacherStudentList(teacher.getUserId());

        assertEquals(3, list.size());
    }

    @DisplayName("3. 학교로 선생님 찾기")
    @Test
    void test_3(){
        userTestHelper.createTeacher(school, "teacher2");
        userTestHelper.createTeacher(school, "teacher3");

        List<User> list = userService.findBySchoolTeacherList(school.getSchoolId());

        assertEquals(3,list.size());
    }
}
