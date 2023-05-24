package com.atguigu.gulimail.order;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.*;

@SpringBootTest
@Slf4j
@RunWith(value = SpringRunner.class)
class GulimailOrderApplicationTests {

    public static final String queueName = "hello-queue";
    public static final String exchangeName = "hello-direct-exchange";
    public static final String routingKey = "hello.world";

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    String a = "111";

    /**
     * 1.创建一个DirectExchange交换机
     * public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
     * name：交换机名称
     * durable：是否持久化(开启的话重启也能存在否则重启就没了) 默认是true开启的
     * autoDelete：是否自动删除（没有队列绑定就删了）
     * arguments：自定义参数
     */
    @Test
    void createExchange() {
        DirectExchange directExchange = new DirectExchange(exchangeName);
        amqpAdmin.declareExchange(directExchange);
        log.info("创建交换机[{}]成功",exchangeName);
    }

    /**
     * 2.创建queue的参数解释
     * param name：队列的名称——不能为空;设置为“”以让代理生成名称。
     * param durable：是否持久化，默认true：持久队列(该队列在服务器重启后仍然有效)
     * param exclusive：默认false,如果我们声明的是排他性队列(该队列仅由声明者的连接使用,其他连接不能使用，默认不可以设置)
     * param autoDelete：默认false,如果服务器应该在队列不再使用时删除队列，则为true
     * param arguments：用于声明队列的参数
     */
    @Test
    void createQueue() {
        Queue queue = new Queue(queueName);
        String s = amqpAdmin.declareQueue(queue);
        log.info("创建队列[{}]成功",queueName);
    }

    /**
     * 3.创建Binding参数解释
     *  destination：目的地
     *  destinationType：目的地类型
     *  exchange：交换机
     *  routingKey：路由键
     *  arguments：参数
     */
    @Test
    void creatBinding(){
        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE,exchangeName,routingKey,null);
        amqpAdmin.declareBinding(binding);
        log.info("创建绑定[{}]成功","hello-Binding");
    }

    /**
     * 4.开始测试发送消息
     * 1.如果是对象 写成二进制流 所以必须实现 implements Serializable 序列化
     * 2.可以转JSONString发送
     * 3.或者修改消息体的序列化机制json序列化，这样我们实体对象可以直接发送，并且不需要实现Serializable，需要自定义配置MqConfig
     */
    @Test
    void testSendMsg(){
        Stu stu = new Stu();
        stu.setAge(18);
        stu.setName("张三");
        stu.setDate(new Date());
        stu.setA(Arrays.asList("1","2","3"));
        rabbitTemplate.convertAndSend(exchangeName,routingKey, stu);
        log.info("消息发送[{}]成功",stu);
    }

}
