package com.atguigu.common.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 解决两件事：
 *  1.存放redis数据序列化为json
 *  2.解决跨域session共享
 */
@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("gulimail.com");
//        defaultCookieSerializer.setDomainNamePattern("*.gulimail.com");
        return defaultCookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(){
        return new GenericFastJsonRedisSerializer();
    }

}
