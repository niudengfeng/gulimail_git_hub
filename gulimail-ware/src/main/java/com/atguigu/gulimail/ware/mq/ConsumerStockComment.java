package com.atguigu.gulimail.ware.mq;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.constants.MqConstants;
import com.atguigu.common.dto.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.OrderLockVO;
import com.atguigu.common.vo.OrderVo;
import com.atguigu.gulimail.ware.dao.WareSkuDao;
import com.atguigu.gulimail.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimail.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimail.ware.feign.OrderFeign;
import com.atguigu.gulimail.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimail.ware.service.WareOrderTaskService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class ConsumerStockComment {

    @Autowired
    private WareOrderTaskDetailService detailService;

    @Autowired
    private WareOrderTaskService taskService;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private WareSkuDao wareSkuDao;

    @RabbitListener(queues = MqConstants.stockReleaseQueue)
    public void ReleaseStock(Message message, Channel channel, StockLockedTo to,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("处理锁定库存的消息：看是否需要回滚");
        Long taskId = to.getTaskId();
        Long taskDetailId = to.getTaskDetailId();
        WareOrderTaskDetailEntity detail = detailService.getById(taskDetailId);
        WareOrderTaskEntity task = taskService.getById(taskId);
        if (task!=null && detail!=null){
            //所库存成功了。需要处理
            //继续判断订单是否创建成功，1.如果没有订单 需要回滚库存  2.有订单，需要判断状态，已取消需要解锁库存 否则不处理
            R r = orderFeign.getOrderStatus(task.getOrderSn());
            if (r.getCode() == 0){
                Object o = r.get("order");
                OrderVo orderVo = JSON.parseObject(JSON.toJSONString(o), OrderVo.class);
                if (orderVo!=null && orderVo.getStatus() != 4){
                    //订单存在并且不是无效订单，无需解锁
                    deLock(detail);
                    channel.basicAck(deliveryTag,false);
                }else {
                    //订单不存在或者已取消订单，需要回滚库存，然后消费掉
                    unLockStock(detail);
                    channel.basicAck(deliveryTag,false);
                }
            }else {
                //接口调用失败了，需要重归队列
                channel.basicReject(deliveryTag,true);
            }

        }else {
            //锁定库存失败了，无需处理,消费掉这条消息，手动确认
            deLock(detail);
            channel.basicAck(deliveryTag,false);
        }
    }

    /**
     * 回滚库存
     * @param detail
     */
    private void unLockStock(WareOrderTaskDetailEntity detail){
        wareSkuDao.unLock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
    }

    /**
     * 正常情况，需要更改taskDetail的扣减状态
     * @param detail
     */
    private void deLock(WareOrderTaskDetailEntity detail){
        detail.setLockStatus(2);
        detailService.save(detail);
    }
}
