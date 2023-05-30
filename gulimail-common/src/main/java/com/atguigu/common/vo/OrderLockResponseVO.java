package com.atguigu.common.vo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class OrderLockResponseVO implements Serializable {
    private Long skuId;
    private int count;
    private Boolean lock;
}
