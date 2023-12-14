package com.sp.fc.user.service;

import com.sp.fc.user.domain.Authority;
import com.sp.fc.user.domain.User;
import com.sp.fc.user.repository.UserRepository;
import com.sp.fc.user.service.helper.UserTestHelper;
import com.sp.fc.user.service.helper.WithUserTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**

 사용자 생성
 이름 수정
 권한 부여
 권한 취소
 email검색
 role 중복해서 추가되지 않는다.
 email이 중복되어서 들어가는가?

 */
@DataJpaTest
public class UserTest extends WithUserTest {

    @BeforeEach
    protected void before(){prepareUserServices();}

    @DisplayName("1. user 생성하기")
    @Test
    void test_1(){
        User user = UserTestHelper.makeUser(school, "user1");
        userService.save(user);

        List<User> list = userRepository.findAll();

        assertEquals(1,list.size());
        UserTestHelper.assertUser(user.getSchool(),list.get(0),"user1");
    }

    @DisplayName("2. Id로 유저찾기")
    @Test
    void test_2(){
        User user = UserTestHelper.makeUser(school, "user1");
        userService.save(user);

        User user2 = userService.findById(user.getUserId()).get();

        assertNotNull(user2);
        UserTestHelper.assertUser(school,user2,"user1");
    }

    @DisplayName("3. 권한 주기")
    @Test
    void test_3(){
        User user = UserTestHelper.makeUser(school, "user1");
        userService.save(user);
        userService.addAuthority(user.getUserId(),Authority.ROLE_STUDENT);

        user = userService.findById(user.getUserId()).get();

        UserTestHelper.assertUser(school,user,"user1",Authority.ROLE_STUDENT);

        userService.addAuthority(user.getUserId(), Authority.ROLE_TEACHER);

        UserTestHelper.assertUser(school, user, "user1", Authority.ROLE_STUDENT,Authority.ROLE_TEACHER);
    }

    @DisplayName("4. 권한 뺏기")
    @Test
    void test_4(){
        User user = UserTestHelper.makeUser(school,"user1");
        userService.save(user);
        userService.addAuthority(user.getUserId(),Authority.ROLE_TEACHER);
        userService.addAuthority(user.getUserId(),Authority.ROLE_STUDENT);

        user = userService.findById(user.getUserId()).get();
        UserTestHelper.assertUser(school,user,"user1",Authority.ROLE_TEACHER,Authority.ROLE_STUDENT);
        assertEquals(2,user.getAuthorities().size());

        userService.removeAuthority(user.getUserId(),Authority.ROLE_STUDENT);

        UserTestHelper.assertUser(school,user,"user1",Authority.ROLE_TEACHER);
        assertEquals(1,user.getAuthorities().size());
    }

    @DisplayName("5. user 이름 업데이트하기")
    @Test
    void test_5(){
        User user = userTestHelper.createUser(school, "user1");

        userService.updateUsername(user.getUserId(),"user2");

        user = userService.findById(user.getUserId()).get();

        assertEquals(user.getName(),"user2");

//        UserTestHelper.assertUser(school,user,"user2");
    }

    @DisplayName("6. email 로 user 찾기")
    @Test
    void test_6(){
        User user = userTestHelper.createUser(school, "user1");

        User user2 = userService.findUserByEmail("user1@test.com").get();

        UserTestHelper.assertUser(school,user2,"user1");
    }

    @DisplayName("7. 권한이 중복으로 들어가지 않는다.")
    @Test
    void test_7(){
        User user = userTestHelper.createUser(school,"user1");

        userService.addAuthority(user.getUserId(),Authority.ROLE_STUDENT);
        userService.addAuthority(user.getUserId(),Authority.ROLE_STUDENT);

        user = userService.findById(user.getUserId()).get();
        assertEquals(1,user.getAuthorities().size());
    }

    @DisplayName("8. email 이 중복되면?")
    @Test
    void test_8(){
        userTestHelper.createUser(school,"user1");

        assertThrows(DataIntegrityViolationException.class, ()->{
            userTestHelper.createUser(school,"user1");
        });

    }
}
