package com.atguigu.gulimail.gateway;

import com.atguigu.common.config.SentinelConfig;
import com.atguigu.common.config.SessionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
public class GulimailGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailGatewayApplication.class, args);
    }

}
