package com.atguigu.common.utils;

public enum BusinessCode {
    ERROR(10001,"系统未知失败"),
    SMSSENDFAST(10002,"验证码获取频繁，请稍后再试"),
    VALIDERROR(10003,"参数校验失败"),
    VALIDSMSCODEERROR(10004,"验证码校验失败"),
    NOSENDSMSCODE(10005,"请先发送短信验证码"),
    LOGINERROR(10006,"登录名与密码不匹配"),
    LOGINERRORNONE(10007,"当前用户不存在");

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
