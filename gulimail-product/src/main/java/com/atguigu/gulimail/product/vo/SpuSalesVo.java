package com.atguigu.gulimail.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuSalesVo{
    private Long attrId;
    private String attrName;
    private List<AttrValueSkuIdVo> valueSkuIdVos;
}
