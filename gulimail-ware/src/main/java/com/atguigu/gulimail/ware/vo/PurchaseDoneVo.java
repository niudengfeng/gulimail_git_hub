package com.atguigu.gulimail.ware.vo;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ToString
public class PurchaseDoneVo {

    /**
     * {
     *    id: 123,//采购单id
     *    items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     * }
     */
    @NotEmpty
    private Long id;//采购单id

    private List<PurchaseDetailDoneVo> items;
}
