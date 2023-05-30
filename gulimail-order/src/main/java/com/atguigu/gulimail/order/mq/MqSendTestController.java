package com.atguigu.gulimail.order.mq;


import com.atguigu.common.constants.MqConstants;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.Stu;
import com.atguigu.gulimail.order.entity.Teacher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/mq")
@Api(tags = "mq测试类")
public class MqSendTestController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendSms/{num}")
    @ApiOperation(value = "发送消息测试")
    public String sendMq(@PathVariable("num") int num){
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0){
                sendOuShu(i);
            }else {
                sendTeacher(i);
            }
        }
        return "ok";
    }

    private void sendTeacher(int i) {
        Teacher stu = new Teacher();
        stu.setAge(18+ i);
        stu.setName("老师"+ i);
        String id = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(MqConstants.TESTEXCHANGENAME,MqConstants.TESTROUTINGKEY, stu,new CorrelationData(id));
        log.info("消息发送[{}]成功",stu);
    }

    private void sendOuShu(int i) {
        Stu stu = new Stu();
        stu.setAge(18+ i);
        stu.setName("学生"+ i);
        stu.setDate(new Date());
        stu.setA(Arrays.asList("1","2","3"));
        String id = UUID.randomUUID().toString();
        rabbitTemplate.convertAndSend(MqConstants.TESTEXCHANGENAME,MqConstants.TESTROUTINGKEY, stu,new CorrelationData(id));
        log.info("消息发送[{}]成功",stu);
    }


    @GetMapping("/testYsSend")
    @ApiOperation("测试延时队列发送")
    public String testYs(){
        String orderId = UUID.randomUUID().toString();
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderId);
        orderEntity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend(MqConstants.orderEventExchange,
                MqConstants.orderCreateOrderRoutingKey,orderEntity,
                new CorrelationData(orderId));
        return "ok";
    }
}
