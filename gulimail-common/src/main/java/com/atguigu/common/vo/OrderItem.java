package com.atguigu.common.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OrderItem {

    private Long skuId;
    private int count;
    private String skuName;

}
