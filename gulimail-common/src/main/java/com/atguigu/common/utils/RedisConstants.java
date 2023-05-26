package com.atguigu.common.utils;

public class RedisConstants {

    //短信验证码的redis前缀
    public static final String SMS_CODE_PREFIX = "SMS:CODE:";

    public static final String SESSION_USER_KEY = "user";
    public static final String SESSION_USER_KEY_NAME = "user-key";
    public static final String CART_USER_KEY_PREFIEX = "CART:USER:";
    public static final String ORDER_TOKEN_USER_PREFIX = "order:token:";

    public static final int SESSION_USER_KEY_TIMEOUT = 30*24*60*60;//30天

}
