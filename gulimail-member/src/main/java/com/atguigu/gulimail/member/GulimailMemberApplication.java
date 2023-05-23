package com.atguigu.gulimail.member;

import com.atguigu.common.config.SessionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession//开启spring session
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Import(SessionConfig.class)
public class GulimailMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailMemberApplication.class, args);
    }

}
