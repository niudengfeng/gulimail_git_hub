package com.atguigu.gulimail.order;

import com.atguigu.common.config.SessionConfig;
import com.atguigu.gulimail.order.entity.Stu;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    public static final String queueName = "hello-queue";

    public static void main(String[] args) {
        SpringApplication.run(GulimailOrderApplication.class, args);
    }


    /**
     * 监听mq队列消息
     * 特性：可以有很多服务一起来监听这个队列，只要收到消息，队列删除消息，而且只能有一个服务去消费这条消息
     * 场景模拟：
     *  1.启动多个服务一起去监听同一个队列：同一个消息只会被消费一次。不会重复消费
     *
     * @param message 消息包装类
     * @param stu  T 自定义的消息
     * @param channel 通道信息
     *
     */
    @RabbitListener(queues = queueName)
    public void receive(Message message, Stu stu, Channel channel){
        System.out.println("接收到消息message:"+message);
        System.out.println("接收到消息头:"+message.getMessageProperties().getHeaders());
        System.out.println("接收到消息体:"+new String(message.getBody()));
        System.out.println("接收到消息自定义对象Stu:"+stu);
    }


}
