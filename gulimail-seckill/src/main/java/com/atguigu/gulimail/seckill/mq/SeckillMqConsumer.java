package com.atguigu.gulimail.seckill.mq;

import cn.hutool.core.date.DateUtil;
import com.atguigu.common.constants.MqConstants;
import com.atguigu.common.vo.seckill.SeckillOrderVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = {MqConstants.seckillQueue})
public class SeckillMqConsumer {

    /**
     * TODO 创建订单和订单项
     * @param message
     * @param seckillOrderVo
     * @param channel
     * @param deliveryTag
     */
    @RabbitHandler
    public void seckillOrderCreate(Message message, SeckillOrderVo seckillOrderVo,
                                   Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info(DateUtil.now() +"接收到秒杀订单："+seckillOrderVo);
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
