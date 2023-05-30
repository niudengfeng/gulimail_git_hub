package com.atguigu.common.dto.mq;

import lombok.Data;
import lombok.ToString;


@Data
@ToString
public class StockLockedTo {
    private Long taskId;//工作单ID
    private Long taskDetailId;//工作单详情ID
}
