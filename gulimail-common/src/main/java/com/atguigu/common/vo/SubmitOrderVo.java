package com.atguigu.common.vo;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 提交订单VO
 */
@Data
@ToString
public class SubmitOrderVo {

    //所选收货地址的ID
    private Long addrId;
    //支付方式
    private String payType;
    //无需提交商品信息。因为用户可能停留很久在提交，这个时候商品数据可能发生变化，比如价格 库存，所以应该去redis获取购物车，然后实时查询所需信息
    private String orderToken;
    //应付总额 验证价格使用
    private BigDecimal payAmount;
    //用户信息去session取
    //备注信息
    private String bak;
}
