package com.atguigu.gulimail.order.vo;

import com.atguigu.gulimail.order.entity.OrderEntity;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 提交订单VO
 */
@Data
@ToString
public class SubmitOrderResponseVo {

    private OrderEntity order;

    private Integer code;//错误码

}
