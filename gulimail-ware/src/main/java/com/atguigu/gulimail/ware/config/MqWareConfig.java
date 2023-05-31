package com.atguigu.gulimail.ware.config;

import com.atguigu.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MqWareConfig {

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue(){
        HashMap<String, Object> arg = new HashMap<>();
        arg.put("x-dead-letter-exchange", MqConstants.stockEventExchange);//指定死性交换机
        arg.put("x-dead-letter-routing-key",MqConstants.stockReleaseRoutingKey);//指定死性routingkey,过期后回传给交换机以这个routingKey
        arg.put("x-message-ttl",120*1000);//毫秒 2分钟
        Queue queue = new Queue(MqConstants.stockDelayQueue,true,false,false,arg);
        return queue;
    }

    /**
     * 死性队列
     * @return
     */
    @Bean
    public Queue stockRealseQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        Queue queue = new Queue(MqConstants.stockReleaseQueue,true,false,false,null);
        return queue;
    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange(MqConstants.stockEventExchange,true,false);
        return topicExchange;
    }

    /**
     * 死性队列和交换机绑定 用 orderReleaseOrderRoutingKey
     * @return
     */
    @Bean
    public Binding stockReleaseBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        Binding binding = new Binding(MqConstants.stockReleaseQueue, Binding.DestinationType.QUEUE,
                MqConstants.stockEventExchange,MqConstants.stockReleaseRoutingKey,null);
        return binding;
    }

    /**
     * 延时队列和交换机绑定 用 orderCreateOrderRoutingKey
     * @return
     */
    @Bean
    public Binding stockDelayBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        Binding binding = new Binding(MqConstants.stockDelayQueue, Binding.DestinationType.QUEUE,
                MqConstants.stockEventExchange,MqConstants.stockLockRoutingKey,null);
        return binding;
    }
}
