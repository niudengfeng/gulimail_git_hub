package com.atguigu.gulimail.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@ApiModel(description = "调用支付宝支付的业务请求参数")
@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class AliPayRequest {

    @ApiModelProperty(value = "本地系统的支付订单标识")
    private String outTradeNo;
    @ApiModelProperty(value = "订单总金额。 单位为元，精确到小数点后两位")
    private BigDecimal totalAmount;
    @ApiModelProperty(value = "订单标题")
    private String subject;

}
