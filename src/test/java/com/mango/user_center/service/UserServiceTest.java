package com.mango.user_center.service;

import com.mango.user_center.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

//    @Test
//    public void testAddUser(){
//        User user = new User();
//        user.setUsername("mango");
//        user.setUserAccount("114514");
//        user.setAvatarUrl("https://i0.hdslb.com/bfs/face/f2c94a26083819d666a01fddbf1ff820e3999451.jpg@96w_96h_1c_1s_!web-avatar.avif");
//        user.setGender(0);
//        user.setPassword("123456");
//        user.setPhone("18076396971");
//        user.setEmail("14616@qq.com");
//        boolean result = userService.save(user);
//        System.out.println(user.getId());
//        Assertions.assertTrue(result);
//    }

//    @Test
//    void userRegister() {
//        String userAccount = "mangoman";
//        String userPassword = "";
//        String checkPassword = "123456";
//        String userCode = "1";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "go";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "mango";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
//        Assertions.assertEquals(-1, result);
//
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
//        Assertions.assertEquals(-1, result);
//
//        userAccount = "mangomana";
//        checkPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
//        Assertions.assertTrue(result > 0);
//    }
}