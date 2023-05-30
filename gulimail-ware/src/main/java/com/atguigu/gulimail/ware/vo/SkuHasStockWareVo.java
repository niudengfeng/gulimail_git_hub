package com.atguigu.gulimail.ware.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuHasStockWareVo {

    private Long skuId;
    private int count;
    private List<Long> wareIds;

}
