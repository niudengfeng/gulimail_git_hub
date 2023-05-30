package com.atguigu.gulimail.order.config;

import com.atguigu.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MqOrderCreateOrderConfig {

    /**
     * 延时队列
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        HashMap<String, Object> arg = new HashMap<>();
        arg.put("x-dead-letter-exchange", MqConstants.orderEventExchange);//指定死性交换机
        arg.put("x-dead-letter-routing-key",MqConstants.orderReleaseOrderRoutingKey);//指定死性routingkey,过期后回传给交换机以这个routingKey
        arg.put("x-message-ttl",60*1000);//毫秒 1分钟
        Queue queue = new Queue(MqConstants.orderDelayQueue,true,false,false,arg);
        return queue;
    }

    /**
     * 死性队列
     * @return
     */
    @Bean
    public Queue orderRealseQueue(){
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        Queue queue = new Queue(MqConstants.orderReleaseQueue,true,false,false,null);
        return queue;
    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange(MqConstants.orderEventExchange,true,false);
        return topicExchange;
    }

    /**
     * 死性队列和交换机绑定 用 orderReleaseOrderRoutingKey
     * @return
     */
    @Bean
    public Binding orderReleaseBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        Binding binding = new Binding(MqConstants.orderReleaseQueue, Binding.DestinationType.QUEUE,
                MqConstants.orderEventExchange,MqConstants.orderReleaseOrderRoutingKey,null);
        return binding;
    }

    /**
     * 延时队列和交换机绑定 用 orderCreateOrderRoutingKey
     * @return
     */
    @Bean
    public Binding orderDelayBinding(){
        //String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        Binding binding = new Binding(MqConstants.orderDelayQueue, Binding.DestinationType.QUEUE,
                MqConstants.orderEventExchange,MqConstants.orderCreateOrderRoutingKey,null);
        return binding;
    }
}
