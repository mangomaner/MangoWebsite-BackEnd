package com.mango.user_center.controller;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mango.user_center.common.BaseResponse;
import com.mango.user_center.common.ErrorCode;
import com.mango.user_center.common.ResultUtils;
import com.mango.user_center.exception.BusinessException;
import com.mango.user_center.model.domain.User;
import com.mango.user_center.model.domain.request.UserChangeAvatarRequest;
import com.mango.user_center.model.domain.request.UserChangeNameRequest;
import com.mango.user_center.model.domain.request.UserLoginRequest;
import com.mango.user_center.model.domain.request.UserRegisterRequest;
import com.mango.user_center.service.UserService;
import com.mango.user_center.utils.GiteeImgBed;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mango.user_center.constant.UserConstant.ADMIN_ROLE;
import static com.mango.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController //返回值默认为json格式 = @Controller + @ResponseBody
@RequestMapping("/user")
@Transactional(rollbackFor = Exception.class)
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userCode = userRegisterRequest.getUserCode();

        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userCode)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword, userCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.SUCCESS);
        }
        long userId = currentUser.getId();
        User safetyUser = userService.getCurrentUser(userId, request);

        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request){
        if(!isAdmin(request)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/changeName")
    public BaseResponse<String> changeName(@RequestBody UserChangeNameRequest userChangeNameRequest, HttpServletRequest request){
        if(userChangeNameRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        String userAccount = userChangeNameRequest.getUserAccount();
        String changeName = userChangeNameRequest.getChangeName();

        if(StringUtils.isAnyBlank(userAccount, changeName)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "昵称不能为空");
        }

        String name = userService.userChangeName(userAccount, changeName, request);
        return ResultUtils.success(name);
    }

    @PostMapping("/changeAvatar")   //阿里云上传文件
    public BaseResponse<String> changeAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userAccount") String userAccount) throws Exception{
        String url = userService.changeAvatar(file, userAccount);
        return ResultUtils.success(url);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        if(user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return true;
    }

}





//    gitee图床
//    @PostMapping("/uploadAvatar")
//    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception{
//        String originaFileName = file.getOriginalFilename();
//        //上传图片不存在时
//        if(originaFileName == null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传失败");
//        }
//
//        String suffix = originaFileName.substring(originaFileName.lastIndexOf("."));
//        //设置图片名字
//        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + suffix;
//
//        String paramImgFile = Base64.encode(file.getBytes());
//        //设置转存到Gitee仓库参数
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("access_token", GiteeImgBed.ACCESS_TOKEN);
//        paramMap.put("message", GiteeImgBed.ADD_MESSAGE);
//        paramMap.put("content", paramImgFile);
//
//        //转存文件路径
//        String targetDir = GiteeImgBed.PATH + fileName;
//        //设置请求路径
//        String requestUrl = String.format(GiteeImgBed.CREATE_REPOS_URL, GiteeImgBed.OWNER,
//                GiteeImgBed.REPO_NAME, targetDir);
//
//        String resultJson = HttpUtil.post(requestUrl, paramMap);
//        JSONObject jsonObject = JSONUtil.parseObj(resultJson);
//        //表示操作失败
//        if (jsonObject==null || jsonObject.getObj("commit") == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//
//        JSONObject content = JSONUtil.parseObj(jsonObject.getObj("content"));
//        String downloadUrl = content.getStr("download_url");
//        return ResultUtils.success(downloadUrl);
//    }


