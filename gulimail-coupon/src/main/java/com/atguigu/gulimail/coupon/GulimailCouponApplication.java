package com.atguigu.gulimail.coupon;

import com.atguigu.common.config.SentinelConfig;
import com.atguigu.common.config.SessionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession//开启spring session
@Import({SessionConfig.class, SentinelConfig.class})
@EnableDiscoveryClient
@SpringBootApplication
public class GulimailCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailCouponApplication.class, args);
    }

}
