package com.atguigu.gulimail.auth;

import com.atguigu.common.config.SessionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 开启spring session
 * 1.引入依赖
 *         <dependency>
 *             <groupId>org.springframework.session</groupId>
 *             <artifactId>spring-session-data-redis</artifactId>
 *         </dependency>
 * 2.启动类上开启注解
 * @EnableRedisHttpSession
 * 3.配置文件加上相关配置
 * spring.session.store-type=redis //指定缓存到redis
 * server.servlet.session.timeout=60m //缓存失效时间是60分钟
 * 4.controller方法上加上入参HttpSession,方法调用session.setAttribute("user",r.get("user")); 就会把user加入到redis中去
 *
 *
 **SpringSession 核心原理 利用过滤器，重写getSession();SessionRepository
 * *1)、@EnableRedisHttpSession导入RedisHttpSessionConfiguration配置
 *      1.1、给容器中添加了一个组件SessionRepository = 》》》[RedisOperationsSessionRepository] ==》redis操作session。 session的增删改查
 * 2、SessionRepositoryFilter == 》Filter: session'存储过滤器，每个请求过来都必须经过filter
     * 2.1、创建的时候，就自动从容器中获取到了sessionRepository;
     * 2.2、原始的request，response都被包装。SessionRepositoryRequestWrapper， SessionRepositoryResponseWrapper
 * 3、以后获取session。request.getSession();SessionRepositoryRequestwrapper
 * 4、wrappedRequest.getSession();===> SessionRepository 中获取到的。
 * 装饰者模式;
 * 自动延期，redis中的数据也是有过期时间
 */
@EnableRedisHttpSession//开启spring session
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@Import(SessionConfig.class)
@EnableSwagger2
@Slf4j
public class GulimailAuthApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(GulimailAuthApplication.class, args);
        ConfigurableEnvironment env = run.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n\t" +
                        "SwaggerUI: \thttp://localhost:{}/swagger-ui.html\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.port"));
    }

}
