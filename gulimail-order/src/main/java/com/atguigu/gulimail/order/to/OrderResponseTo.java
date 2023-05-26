package com.atguigu.gulimail.order.to;

import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.OrderItemEntity;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString
public class OrderResponseTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItemEntityList;
    private BigDecimal payAmount;//应付金额
    private BigDecimal fare;//运费
}
