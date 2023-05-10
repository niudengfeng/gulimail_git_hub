package com.atguigu.gulimail.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexCatelogGoryVo {

    private String catalog1Id;

    private String id;

    private String name;

    private List<CateGory3Vo> catalog3List;
}
