package com.atguigu.gulimail.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@ApiModel(description = "调用支付宝转账提现接口请求参数")
@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class AliPayTransRequest {

    @ApiModelProperty(value = "本地系统的提现流水号标识")
    private String outBizNo;
    @ApiModelProperty(value = "转账金额，单位为元")
    private BigDecimal amount;
    @ApiModelProperty(value = "用户支付宝的openId")
    private String openId;
}
