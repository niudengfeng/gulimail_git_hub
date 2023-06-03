package com.atguigu.common.vo.seckill;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class SeckillOrderVo {

    private String orderSn;
    private Long skuId;
    private Long prosessionId;//场次ID
    private Long memberId;
    private BigDecimal seckillPrice;//秒杀价格
    private Integer count;//购买数量

}
