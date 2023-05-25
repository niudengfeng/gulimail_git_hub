package com.atguigu.gulimail.order.mq;

import com.atguigu.gulimail.order.entity.Stu;
import com.atguigu.gulimail.order.entity.Teacher;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = MqConstants.TESTQUEUENAME)//监听队列
@Component
public class MqConsumerCompoment {

    @RabbitHandler
    public void consumeTeacher(Message message, Teacher teacher, Channel channel) throws IOException {
        System.out.println("接收到消息自定义对象Teacher:"+teacher);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //第一个参数是个自动递增的序号，第二个true代表是否批量处理，建议不要批量，来一个确认一个
        channel.basicAck(deliveryTag,false);
        //第一个参数是个自动递增的序号，第二个true代表是否批量处理，建议不要批量，来一个确认一个，第三个参数代表是否重回队列，如果是，那么可以重新消费，建议入库，这里错误就丢弃
//        channel.basicNack(deliveryTag,false,true);
    }

    @RabbitHandler
    public void consumeStu(Message message, Stu stu, Channel channel) throws IOException {
        System.out.println("接收到消息自定义对象Stu:"+stu);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        channel.basicAck(deliveryTag,false);//第一个参数是个自动递增的序号，第二个true代表是否批量处理，建议不要批量，来一个确认一个
    }
}
