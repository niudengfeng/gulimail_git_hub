package com.atguigu.gulimail.seckill.config;

import cn.hutool.core.date.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;

@Configuration
public class MqConfig {

//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    /**
     * 保证我们发送对象，会自动转成json存入mq
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


}
