package com.atguigu.gulimail.seckill.config;

import com.atguigu.common.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MqSeckillConfig {

    /**
     * 队列
     * @return
     */
    @Bean
    public Queue seckillQueue(){
        Queue queue = new Queue(MqConstants.seckillQueue,true,false,false,null);
        return queue;
    }

    /**
     * 交换机
     * @return
     */
    @Bean
    public Exchange seckillEventExchange(){
        TopicExchange topicExchange = new TopicExchange(MqConstants.seckillEventExchange,true,false);
        return topicExchange;
    }

    /**
     * 绑定
     * @return
     */
    @Bean
    public Binding seckillBinding(){
        Binding binding = new Binding(MqConstants.seckillQueue, Binding.DestinationType.QUEUE,
                MqConstants.seckillEventExchange,MqConstants.seckillRoutingKey,null);
        return binding;
    }
}
