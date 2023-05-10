package com.atguigu.common.page;

import lombok.Data;

@Data
public class PageResult {

    private Integer pageNum;//当前页码
    private Integer totalPages;//总页数
    private Integer total;//总记录数
}
