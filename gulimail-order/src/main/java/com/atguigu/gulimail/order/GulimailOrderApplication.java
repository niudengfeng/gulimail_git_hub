package com.atguigu.gulimail.order;

import com.atguigu.common.config.SessionConfig;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * *使用RabbitMO
 * 测试代码参考测试类：GulimailOrderApplicationTests
 * 1、引入amqp场景;
 *      RabbitAutoConfiguration 就会自动生效
 * 2.给容器中自动配置了RabbitTemplate、AmpAdmin、CachingConnectionFactory、RabbitMessagingTemplate;
 *      所有的属性都是 spring.rabbitmq
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 * 3.给配置文件中配置 spring.rabbitmg 信息
 * 4.开启注解@EnableRabbit: @EnableXxxxx; 开启功能
 */
@EnableRedisHttpSession//开启spring session
@Import(SessionConfig.class)
@EnableDiscoveryClient
@SpringBootApplication
@EnableRabbit
public class GulimailOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailOrderApplication.class, args);
    }

}
