package com.atguigu.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class OrderLockVO implements Serializable {

    private String orderSn;

    private List<OrderItem> orderItems;

}
