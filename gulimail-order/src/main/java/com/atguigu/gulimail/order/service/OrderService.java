package com.atguigu.gulimail.order.service;

import com.atguigu.gulimail.order.vo.SubmitOrderResponseVo;
import com.atguigu.common.vo.SubmitOrderVo;
import com.atguigu.gulimail.order.vo.OrderVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:00:44
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderVo toTrade();

    SubmitOrderResponseVo createOrder(SubmitOrderVo submitOrderVo);

    OrderEntity getOrderStatus(String orderSn);
}

