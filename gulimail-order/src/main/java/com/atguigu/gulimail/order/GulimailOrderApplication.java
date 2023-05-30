package com.atguigu.gulimail.order;

import com.atguigu.common.config.SessionConfig;
import com.atguigu.gulimail.order.entity.Stu;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * *使用RabbitMO
 * 测试代码参考测试类：GulimailOrderApplicationTests
 * 1、pom.xml引入spring-boot-starter-amqp;
 *          <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-amqp</artifactId>
 *         </dependency>
 *      RabbitAutoConfiguration 就会自动生效
 * 2.给配置文件中配置 spring.rabbitmg 信息
 *      #rabbitmq
             * spring.rabbitmq.host=192.168.101.65
             * spring.rabbitmq.port=5672
             * spring.rabbitmq.username=admin
             * spring.rabbitmq.password=123456
             * spring.rabbitmq.virtual-host=/order
             * #开启mq发送确认机制
             * spring.rabbitmq.publisher-confirm-type=correlated
             * #开启发送端消息抵达队列确认
             * spring.rabbitmq.publisher-returns=true
             * #只要抵达队列会以异步方式优先回调return回调确认方法
             * spring.rabbitmq.template.mandatory=true
             * #手动确认消息
             * spring.rabbitmq.listener.simple.acknowledge-mode=manual
 * 3.启动类上开启注解@EnableRabbit
 * 4.监听消息 参考消息消费类MqConsumerCompoment
 *          @RabbitListener 可加在类和方法上  需要指定队列进行消费
 *          @RabbitHandler  只能标在方法上   重载区分不同消息类型进行消费
 * 6.消息的可靠性投递：发送端确认开启rabbitmq的发送端消息发送确认机制
 *      6.1：添加配置spring.rabbitmq.publisher-confirm-type=correlated
 *      6.2：设置rabbitTemplate的发送消息确认回调
 *             @PostConstruct
         *     public void initTemplate(RabbitTemplate rabbitTemplate){
         *         rabbitTemplate.setConfirmCallback((correlationData , ack, cause)->{
         *             System.out.println("发送消息确认：correlationData【"+correlationData+"】,ack["+ack+"] cause:"+cause);
         *         });
         *     }
 *
 *     以上两步只能保证发送端消息抵达服务器，并不能保证到队列；队列确认需要下面步骤：
 *     6.3：添加2个配置：
                         * #开启发送端消息抵达队列确认
                         * spring.rabbitmq.publisher-returns=true
                         * #只要抵达队列会以异步方式优先回调return回调确认方法
                         * spring.rabbitmq.template.mandatory=true
 *     6.4：需要设置rabbitTemplate的return回调方法
 *
 *  7、消费端确认 (保证每个消息被正确消费，此时才可以broker删除这个消息)
 *      1、默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
 *          问题：我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，宕机了。发生消息丢失
 *          解决：手动确认
 *              a：添加配置spring.rabbitmq.listener.simple.acknowledge-mode=manual
 *              b: 监听消息的地方需要手动调用channel.basicAsk(long,boolean):第一个参数是个自动递增的序号，第二个true代表是否批量处理，建议不要批量，来一个确认一个
 *              c:可以拒收消息，重回队列：channel.basicNack(deliveryTag,false,true);第一个参数是个自动递增的序号，第二个true代表是否批量处理，建议不要批量，来一个确认一个，第三个参数代表是否重回队列，如果是，那么可以重新消费，建议入库，这里错误就丢弃
 *
 *
 *
 * 记录使用Seata分布式事务详细步骤
 * 1.给每个需要回滚的数据库加一个undo_log表
 * 2.安装事务协调器TC:即seata服务安装包
 * 3.整合seata到我们的项目中
 *  1.引入依赖: 注意下面这个依赖会自动导入个 seata-all-0.7.1  这个版本需要和你安装的seata服务包版本一致
 *          <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
 *         </dependency>
 *   2.解压并启动下载下来的seata-server-0.7.1.zip:
 *          打开文件夹看到两个配置文件
 *              regist.conf : 配置文件：先把regist注册改成nacos
 *              file.conf : 这个先不动
 *   3.所有需要用到分布式事务的微服务使用seata的数据源包装下，配置写在了，MySeataConfig.java文件中，目前给order和ware服务加了
 *   4.每个服务都必须导入
 *           regist.conf : 配置文件：先把regist注册改成nacos
 *           file.conf :service下的 vgroup_mapping.{application.name}-fescar-service-group = "default"
 *   5.启动测试分布式事务
 *   6.给分布式大事务的方法上加个注解@GlobalTransactional
 *   7.每一个小事务加上@Transactional即可
 */
@EnableRedisHttpSession//开启spring session
@Import(SessionConfig.class)
@EnableDiscoveryClient
@SpringBootApplication
@EnableRabbit
@EnableSwagger2
@Slf4j
@EnableFeignClients
public class GulimailOrderApplication {

    public static final String queueName = "hello-queue";

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext run = SpringApplication.run(GulimailOrderApplication.class, args);
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


    /**
     * 监听mq队列消息
     * 特性：可以有很多服务一起来监听这个队列，只要收到消息，队列删除消息，而且只能有一个服务去消费这条消息
     * 场景模拟：
     *  1.启动多个服务一起去监听同一个队列：同一个消息只会被消费一次。不会重复消费
     *  2.只有一个消息处理完，方法执行结束了，才能接收下一个消息
     * @param message 消息包装类
     * @param stu  T 自定义的消息
     * @param channel 通道信息
     *
     */
//    @RabbitListener(queues = queueName)
    public void receive(Message message, Stu stu, Channel channel){
//        System.out.println("接收到消息message:"+message);
//        System.out.println("接收到消息头:"+message.getMessageProperties().getHeaders());
        System.out.println("接收到消息体:"+new String(message.getBody())+"接收到消息自定义对象Stu:"+stu);
    }


}
