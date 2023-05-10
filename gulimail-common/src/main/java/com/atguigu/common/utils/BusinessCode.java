package com.atguigu.common.utils;

public enum BusinessCode {
    ERROR(10001,"系统未知失败"),
    VALIDERROR(10003,"参数校验失败");

    private Integer code;
    private String message;

    BusinessCode(Integer code,String message){
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
