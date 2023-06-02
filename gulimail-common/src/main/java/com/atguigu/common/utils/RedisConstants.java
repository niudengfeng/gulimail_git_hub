package com.atguigu.common.utils;

public class RedisConstants {

    //短信验证码的redis前缀
    public static final String SMS_CODE_PREFIX = "SMS:CODE:";

    public static final String SESSION_USER_KEY = "user";
    public static final String SESSION_USER_KEY_NAME = "user-key";
    public static final String CART_USER_KEY_PREFIEX = "CART:USER:";
    public static final String ORDER_TOKEN_USER_PREFIX = "order:token:";

    public static final int SESSION_USER_KEY_TIMEOUT = 30*24*60*60;//30天

    //秒杀活动场次
    public static final String SECKILL_RELATION_ID_KEY = "seckill:relationIds:";
    //秒杀活动详细信息前缀
    public static final String SECKILL_RELATION_INFO_KEY = "seckill:skus:";
    //秒杀商品的信号量key
    public static final String SECKILL_SEMAPHORE_KEY = "seckill:Semaphore:";
    //秒杀商品的定时任务分布式锁KEY
    public static final String SECKILL_LOCK = "seckill:task:lock";

}
