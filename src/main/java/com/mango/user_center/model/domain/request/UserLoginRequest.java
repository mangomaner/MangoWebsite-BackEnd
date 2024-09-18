package com.mango.user_center.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登陆请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2972434004306119777L;

    private String userAccount;
    private String userPassword;

}
