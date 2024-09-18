package com.mango.user_center.common;

public class ResultUtils {

    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<T>(0, data,"ok");
    }

    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), errorCode.getDescription());
    }
    public static BaseResponse error(ErrorCode errorCode, String message, String description){
        return new BaseResponse<>(errorCode.getCode(), message, description);
    }
    public static BaseResponse error(int code, String message, String description){
        return new BaseResponse<>(code, null, message, description);
    }
}
