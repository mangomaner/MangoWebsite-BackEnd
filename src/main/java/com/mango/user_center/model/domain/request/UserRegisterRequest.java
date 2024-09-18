package com.mango.user_center.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7284075217827717149L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String userCode;
}
