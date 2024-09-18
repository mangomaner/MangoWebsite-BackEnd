package com.mango.user_center.service;

import com.mango.user_center.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.http.HttpRequest;

/**
* @author mangoman
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-07-27 20:09:19
*/
public interface UserService extends IService<User> {


    /**
     * 用于用户注册功能
     *
     * @param userAccount   账号
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return  返回用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userCode);

    /**
     * 用户登录
     *
     * @param userAccount  账号
     * @param userPassword 密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param oiginUser
     * @return
     */
    User getSafetyUser(User oiginUser);

    /**
     * 用户注销登录
     * @param httpServletRequest
     */
    int userLogout(HttpServletRequest httpServletRequest);

    /**
     * 更改用户昵称
     * @param userAccount   用户名
     * @param changeName    更改昵称
     * @param request
     * @return
     */
    public String userChangeName(String userAccount, String changeName, HttpServletRequest request);

    /**
     * 获取当前用户
     * @param userId
     * @param request
     * @return
     */
    public User getCurrentUser(Long userId, HttpServletRequest request);

    /**
     * 上传用户头像并将url返回给客户端
     * @param file
     * @return
     */
    String changeAvatar(MultipartFile file, String userAccount) throws IOException;
}
