package com.atguigu.gulimail.seckill;


import com.atguigu.common.config.SentinelConfig;
import com.atguigu.common.config.SessionConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 引入sentinel
 *  1.导依赖
 *         <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
 *         </dependency>
 *  2.下载控制台，主题和上面的版本一样，然后运行java -jar 命令
 *  3.微服务配置sentinel地址和传输端口
 *  4.开启sentinel的实时监控 需要引入依赖spring-boot-starter-actuator审计包并配置management.endpoints.web.exposure.include=*
 *  5.可以自定义熔断降级后的返回结果：参考SentinelConfig.class
 *  6.使用sentinel来保护feign的远程调用：
 *                  1.#开启对feign的支持:feign.sentinel.enabled=true
 *                  2.实现feign接口，接口指定fallback类(当调用方手动指定远程服务的降级策略，触发fallback类的回调方法)
 *                  3.针对提供方做接口的降级策略，提供方是在运行，但是不处理业务逻辑，直接触发降级数据（限流数据参考SentinelConfig里面配置的error）
 *
 *   高级使用
 *      1.自定义资源：指定资源名进行限流保护 用法参考：SentinelResourceController
 *          上面这个自定义的好处是，可以在if else 你想要保护的那部分代码上做熔断降级，而不是整个接口全部降级
 *      2.SentinelResource在方法上加上注解，但是不能加在controller层的非@RequestMapping接口方法上，普通方法是感知不到的，需要在service的方法上
 *          1.可以指定资源名，和降级方法 blockHandler指定方法名 需要在一个类下，blockHandlerClass可以指定类下，但是需要静态
 *          2.fallback 配合 fallbackclass可以指定在其他类下但是方法必须static修饰
 *      3.限流策略的配置持久化：默认是内存中，重启就没了。可以考虑持久化到nacos中
 *
 *
 *      3.网关流控：
 *          1.引入依赖：
     *          <dependency>
     *             <groupId>com.alibaba.cloud</groupId>
     *             <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
     *         </dependency>
 *         2.在管理页面可以控制转发服务的routeID进行限流：规则很强大：可以针对url 参数 headler host ip等进行匹配断言，还可以进行分组，对组设置
 *         3.可以对降级数据自定义配置SentinelGatewayConfig，也可以指定返回status 都可以在properties进行配置
 *
 *
 *   链路追踪：  ZIKIN+SLEUTH
 *   1.导入依赖（zipkin依赖包含sleuth的依赖，所以只引入zipkin即可）
 *          <dependency>
 *             <groupId>org.springframework.cloud</groupId>
 *             <artifactId>spring-cloud-starter-zipkin</artifactId>
 *         </dependency>
 *   2.添加配置
 *          #配置zipkin
             * spring.zipkin.base-url=http://192.168.101.65:9411/
             * #关闭服务发现，不需要注册到nacos
             * spring.zipkin.discovery-client-enabled=false
             * spring.zipkin.sender.type=web
             * #采样率默认是0.1 之采样10%,这里采样百分之百
             * spring.sleuth.sampler.probability=1
 *  3.安装zipkin管理后台 docker run -d -p 9411:9411 openzipkin/zipkin
 *  4.启动服务后登录zipkin管理后台查看http://192.168.101.65:9411/
 *  5.持久化：默认是在系统内存，服务重启就没了。一般可以考虑持久化到es中：用下面这个docker命令启动
 *  docker run --env STORAGE_TYPE=elasticsearch --env ES_HOSTS=192.168.101.65:9200 openzipkin/zipkin-dependencies
 *
 */

@EnableRabbit
@EnableRedisHttpSession
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableFeignClients
@MapperScan("com.atguigu.gulimail.seckill.dao")
@EnableSwagger2
@Import({SessionConfig.class, SentinelConfig.class})
@Slf4j
public class SecKillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class,args);
    }
}
