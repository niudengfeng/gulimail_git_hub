package com.atguigu.gulimail.search.vo;

import com.atguigu.common.page.PageParam;
import lombok.Data;

import java.util.List;

/**
 * 定义检索条件
 */
@Data
public class SearchParam extends PageParam {

    //三级分类ID
    private Long catalog3Id;

    //skuTitle 产品名称 模糊搜索
    private String keyword;//ES里面 我们自己约定 skuTitle 用match匹配查询，其余条件用filter过滤 这样更快一点，因为filter不参与评分

    //品牌ID
    private List<Long> brandId;

    //价格区间 _500/500_6000/6000_
    private String skuPrice;

    //是否显示有货 1有 0无
    private Integer hasStock;

    /**属性查询 attrs=1_白色：蓝色*/
    private List<String> attrs;

    //排序 saleCount_asc/desc  skuPrice_asc/desc  hotScore_asc/desc
    private String sort;
}
