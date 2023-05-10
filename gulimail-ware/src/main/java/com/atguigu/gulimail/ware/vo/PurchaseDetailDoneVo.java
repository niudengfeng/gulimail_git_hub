package com.atguigu.gulimail.ware.vo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PurchaseDetailDoneVo {
    //itemId:1,status:4,reason:""
    private Long itemId;//采购项id
    private Integer status;//采购项状态
    private String reason;//如果未完成填写原因
}
