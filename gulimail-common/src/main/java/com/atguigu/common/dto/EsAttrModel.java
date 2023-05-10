package com.atguigu.common.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EsAttrModel {
    private Long attrId;
    private String attrName;
    private String attrValue;
}
