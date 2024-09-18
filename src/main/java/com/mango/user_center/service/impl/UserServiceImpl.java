package com.mango.user_center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mango.user_center.common.ErrorCode;
import com.mango.user_center.config.AliyunConfig;
import com.mango.user_center.exception.BusinessException;
import com.mango.user_center.model.domain.User;
import com.mango.user_center.service.UserService;
import com.mango.user_center.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;
import java.io.InputStream;


import static com.mango.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author mangoman
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-07-27 20:09:19
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private OSS ossClient;

    @Resource
    private AliyunConfig aliyunConfig;

    private static final String SALT = "mango";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userCode) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空", "");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短", "");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短", "");
        }
        //账户不能重复、校验码不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = userMapper.selectCount(queryWrapper);  //可以用this.count
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名重复", "");
        }
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userCode", userCode).ne("userCode", "1145");
        long c = userMapper.selectCount(queryWrapper);
        if(c > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "校验码重复", "");
        }



        //账户不能包含特殊字符
        String validPattern = ".*[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t].*";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符", "");
        }
        // 密码和校验密码相同
        if(!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码与校验密码不同", "");
        }

        //2.加密
        String encryptPassword= DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        user.setUserCode(userCode);
        boolean saveResult = this.save(user);
        if(!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加数据库失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "密码过短");
        }

        //账户不能包含特殊字符
        String validPattern = ".*[[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t].*";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名包含特殊字符");
        }

        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("Password",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if(user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_ERROR, "用户名或密码错误", "");
        }


        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    @Override
    public User getCurrentUser(Long userId, HttpServletRequest request) {
        User user = getById(userId);
        user.setUpdateTime(new Date());
        boolean isUpdated = userMapper.updateById(user) > 0;
        if (!isUpdated) {
            throw new BusinessException(ErrorCode.USER_ERROR, "获取用户失败", "");
        }
        User safetyUser = getSafetyUser(user);
        return safetyUser;
    }

    @Override
    public String changeAvatar(MultipartFile file, String userAccount) throws IOException {
        String url = null;

        //获取上传文件输入流
        InputStream inputStream = file.getInputStream();
        //获取文件名称
        String fileName = file.getOriginalFilename();
        if(fileName == ""){
            throw new BusinessException(ErrorCode.USER_ERROR, "文件名为空", "");
        }
        String extension = fileName.substring(fileName.lastIndexOf("."));


        //保证文件名唯一，去掉uuid中的'-'
        fileName = UUID.randomUUID().toString().replaceAll("-", "") + extension;

//        fileName = uuid + fileName;
//        //把文件按日期分类，构建日期路径：avatar/2019/02/26/文件名
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//        // 获取当前日期
//        LocalDate currentDate = LocalDate.now();
//        // 使用DateTimeFormatter实例将LocalDate对象转换为字符串
//        String datePath = currentDate.format(dtf);
//
//        //拼接
//        fileName = datePath + "/" + fileName;

        //上传到阿里云
        ossClient.putObject(aliyunConfig.getBucketName(), userAccount + "/" + fileName, inputStream);

        //把上传后把文件url返回
        url = "https://" + aliyunConfig.getBucketName() + "." + aliyunConfig.getEndpoint()
                + "/" + userAccount + "/" + fileName;

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_ERROR, "查询失败", "");
        }
        String oldURL = user.getAvatarUrl();
        String oldFileName = oldURL.substring(oldURL.lastIndexOf("/"));
        oldFileName = userAccount + oldFileName;
        ossClient.deleteObject(aliyunConfig.getBucketName(), oldFileName);

        user.setAvatarUrl(url);
        boolean isUpdated = userMapper.updateById(user) > 0;
        if (!isUpdated) {
            throw new BusinessException(ErrorCode.USER_ERROR, "更新头像失败", "");
        }

        return url;
    }

    @Override
    public String userChangeName(String userAccount, String changeName, HttpServletRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_ERROR, "未知错误", "");
        }

        user.setUsername(changeName);
        // 更新数据库
        boolean isUpdated = userMapper.updateById(user) > 0;
        if (!isUpdated) {
            throw new BusinessException(ErrorCode.USER_ERROR, "更新用户名失败", "");
        }

        return changeName;
    }

    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUpdateTime(originUser.getUpdateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserCode(originUser.getUserCode());
        return safetyUser;
    }



    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




