package com.mango.user_center.exception;

import com.mango.user_center.common.ErrorCode;
import lombok.Data;

@Data
public class BusinessException extends RuntimeException{
    private int code;
    private String description;
    public BusinessException(String message, int code, String description){
        super(message);
        this.code = code;
        this.description = description;
    }
    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code =errorCode.getCode();
        this.description=errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description){
        super(errorCode.getMessage());
        this.code =errorCode.getCode();
        this.description=description;
    }

    public BusinessException(ErrorCode errorCode, String message, String description){
        super(message);
        this.code =errorCode.getCode();
        this.description=description;
    }
}
