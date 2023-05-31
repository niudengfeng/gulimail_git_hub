package com.atguigu.common.dto.mq;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Data
@ToString
public class StockLockedTo implements Serializable {
    private Long taskId;//工作单ID
    private Long taskDetailId;//工作单详情ID
}
