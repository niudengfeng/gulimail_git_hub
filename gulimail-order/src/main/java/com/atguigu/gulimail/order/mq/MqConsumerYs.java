package com.atguigu.gulimail.order.mq;

import com.atguigu.common.constants.MqConstants;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
public class MqConsumerYs {

    @RabbitListener(queues = {MqConstants.orderReleaseQueue})
    public void listen(Message message, OrderEntity order, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        System.out.println("当前时间："+new Date()+"收到取消订单消息："+ order.getModifyTime());
        //系统设置了手动确认消息
        channel.basicAck(deliveryTag,false);
    }

}
