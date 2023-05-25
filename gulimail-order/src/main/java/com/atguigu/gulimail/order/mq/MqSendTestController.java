package com.atguigu.gulimail.order.mq;


import com.atguigu.gulimail.order.entity.Stu;
import com.atguigu.gulimail.order.entity.Teacher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class MqSendTestController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendSms/{num}")
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

}
