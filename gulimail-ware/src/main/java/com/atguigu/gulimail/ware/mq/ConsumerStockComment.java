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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RabbitListener(queues = MqConstants.stockReleaseQueue)
public class ConsumerStockComment {

    @Autowired
    private WareOrderTaskDetailService detailService;

    @Autowired
    private WareOrderTaskService taskService;

    @Autowired
    private OrderFeign orderFeign;

    @Autowired
    private WareSkuDao wareSkuDao;

    /**
     * 监听正常库存发出的task工作单消息
     * @param message
     * @param channel
     * @param to
     * @param deliveryTag
     * @throws IOException
     */
    @RabbitHandler
    @Transactional
    public void ReleaseStock(Message message, Channel channel, StockLockedTo to,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("处理锁定库存的消息：看是否需要回滚：to:"+to+",message:"+new String(message.getBody()));
        try {
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
                    if (orderVo==null || orderVo.getStatus() == 4){
                        //订单不存在或者已取消订单，需要回滚库存，然后消费掉
                        if (detail.getLockStatus() == 1){
                            unLockStock(detail);
                        }
                        deLock(detail);//解锁
                    }
                    channel.basicAck(deliveryTag,false);
                }else {
                    //接口调用失败了，需要重归队列
                    log.error("接口远程调用失败了");
//                    channel.basicNack(deliveryTag,false,true);
                }
            }else {
                //锁定库存失败了，无需处理,消费掉这条消息，手动确认
                channel.basicAck(deliveryTag,false);
                deLock(detail);
            }
        } catch (Exception e) {
            //业务处理失败了，需要重回队列
//            channel.basicNack(deliveryTag,false,true);
            log.error("消息消费异常："+e);
        }
    }

    @RabbitHandler
    @Transactional
    public void ReleaseStockClose(Message message, Channel channel, OrderVo orderVo,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("处理订单主动关闭订单的消息：看是否需要回滚：orderVo:"+orderVo);
        //需要先根据订单号查询最新的工作单状态，如果已经在上面处理过了，就不需要处理了，消息消费掉即可
        try {
            QueryWrapper<WareOrderTaskEntity> taskQuery = new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderVo.getOrderSn());
            WareOrderTaskEntity task = taskService.getBaseMapper().selectOne(taskQuery);
            if (task == null){
                log.error("根据当前订单号差不多工作单任务订单号："+orderVo.getOrderSn());
                channel.basicAck(deliveryTag,false);
                return;
            }
            QueryWrapper<WareOrderTaskDetailEntity> detailEntityQueryWrapper = new QueryWrapper<>();
            detailEntityQueryWrapper.eq("task_id",task.getId());
            detailEntityQueryWrapper.eq("lock_status",1);
            List<WareOrderTaskDetailEntity> detailEntities = detailService.getBaseMapper().selectList(detailEntityQueryWrapper);
            if (!CollectionUtils.isEmpty(detailEntities)){
                for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
                    unLockStock(detailEntity);//回滚库存
                    deLock(detailEntity);//工作单详情锁定状态值为解锁了代表库存解锁了
                }
            }
            channel.basicAck(deliveryTag,false);
        } catch (Exception e) {
            log.error("订单主动关闭发送的解锁库存消费消息异常："+e);
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
        detailService.updateById(detail);
    }
}
