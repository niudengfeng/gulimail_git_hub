package com.atguigu.gulimail.order.mq;

import cn.hutool.core.date.DateUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.atguigu.common.constants.MqConstants;
import com.atguigu.gulimail.order.config.AlipayClientConfig;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.service.OrderService;
import com.atguigu.common.vo.OrderVo;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

@Slf4j
@Component
public class MqConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AlipayClientConfig alipayClientConfig;
    @Autowired
    private AlipayClient alipayClient;
    @RabbitListener(queues = {MqConstants.orderReleaseQueue})
    @Transactional
    public void listen(Message message, OrderEntity order, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        System.out.println("关闭订单："+order.getOrderSn()+"当前时间："+ DateUtil.now()+"收到延时订单消息时间："+ DateUtil.format(order.getModifyTime(),"yyyy-MM-dd HH:mm:ss")+"======>"+order);
        try {
            closeOrder(order);
            //关闭订单后立马调用支付宝的关闭订单接口，防止用户支付成功了。
//            closeAlipayOrder(order);

            //系统设置了手动确认消息
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("关闭订单失败,重回队列："+e);
//            channel.basicNack(deliveryTag,false,true);
            throw new RuntimeException(e);
        }
    }

 /*   private void closeAlipayOrder(OrderEntity order) throws AlipayApiException, UnsupportedEncodingException {
        //获得初始化的AlipayClient

        //设置请求参数
        AlipayTradeCloseRequest alipayRequest = new AlipayTradeCloseRequest();
        //商户订单号，商户网站订单系统中唯一订单号
        String out_trade_no = new String(order.getOrderSn().getBytes("ISO-8859-1"),"UTF-8");
        //支付宝交易号
        String trade_no = new String(request.getParameter("WIDTCtrade_no").getBytes("ISO-8859-1"),"UTF-8");
        //请二选一设置
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\"," +"\"trade_no\":\""+ trade_no +"\"}");
        //请求
        String result = alipayClient.execute(alipayRequest).getBody();
        log.info("收到支付宝关闭订单响应："+result);
    }*/


    public void closeOrder(OrderEntity order) throws InterruptedException {
        OrderEntity byId = orderService.getById(order.getId());
        if (byId.getStatus() == 0){//未付款
            byId.setStatus(4);//取消订单，接着解冻库存
            byId.setModifyTime(new Date());
            orderService.updateById(byId);
            OrderVo orderVo = new OrderVo();
            BeanUtils.copyProperties(byId,orderVo);
            //在这边里需要主动告诉库存服务关闭订单了。需要解锁库存，这么做的原因是防止订单服务的卡顿延迟导致 库存的延时逻辑运行在了关闭订单之前。
            //正常业务逻辑是 创建订单后30分钟不付款就关闭订单 然后恢复库存。
            //比如：订单服务 30分钟延时  库存服务 31分钟延时  这样服务运行正常是没问题的。就怕 订单服务卡顿了，没关闭订单，库存服务的延时任务运行了，那个时候订单还没关单，库存恢复不上去的
            /**
             * 模拟场景：
             *          场景1.现在订单1分钟后关闭，紧接着发送库存解锁消息，库存那边处理完，过了接1分钟，处理库存服务自己发送的任务单消息，
             *          场景2：订单1分钟后关闭，睡眠2分钟，发送库存解锁消息，过了1分钟，库存服务延时队列消费结束了，最后订单服务关闭消息发送过来，监听到此消息，去处理，后于库存消费
             */
//            Thread.sleep(2*60*1000);//模拟场景2
            rabbitTemplate.convertAndSend(MqConstants.orderEventExchange,MqConstants.orderCloseRoutingKeyPrefix+order.getOrderSn()
                    ,orderVo,new CorrelationData(byId.getOrderSn()));
        }
    }

}
