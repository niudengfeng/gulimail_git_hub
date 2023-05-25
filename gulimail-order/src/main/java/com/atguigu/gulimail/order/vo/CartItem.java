package com.atguigu.gulimail.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartItem {
    private Long skuId;
    private String skuTitle;
    private String skuDefaultImg;
    private BigDecimal price;
    private Integer count = 1;
    private BigDecimal totalPrice;
    private List<String> skuAttr;//获取当前skuId对应的销售属性
}
