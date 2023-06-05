package com.atguigu.gulimail.ware.config;

import cn.hutool.core.date.DateUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class MqPubConfirm implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate bean = applicationContext.getBean(RabbitTemplate.class);
        if (bean!=null){
            pubConfirm(bean);
        }
    }

    /**
     * 设置rabbitTemplate的发送消息确认回调
     * 比如可以记录发送失败的记录到mysql中
     */

    public void pubConfirm(RabbitTemplate rabbitTemplate){

        /**1.设置消息抵达服务器broken的确认回调方法
         * 只要消息抵达Broker就ack=true
         *      *  correlationData  发送消息时传的唯一ID当前消息的唯一关联数据 (这个是消息的唯一id)
         *      *  ack 发送端确认 Broken服务器是否收到消息《和消息是否被消费无关》
         *      *  cause 失败的原因
         */
        rabbitTemplate.setConfirmCallback((correlationData , ack, cause)->{
            System.out.println(DateUtil.now()+"发送消息确认：correlationData【"+correlationData+"】,ack["+ack+"] cause:"+cause);
        });

        /**
         * 2.设置消息抵达队列的回调方法
         * 这里只有消息发送失败才会进入这个回调方法
         *  message:投递失败的消息内容
         *  replyCode:回复的状态码
         *  replyText：回复的文本内容
         *  exchange：当时发送这个消息指定的交换机
         *  routingKey：当时发送这个消息指定的routingKey
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println(DateUtil.now()+"Fail Message["+message+"] replyCode["+replyCode+"] replyText["+replyText+"] exchange["+exchange+"] routingKey["+routingKey+"]");
        });
    }
}
