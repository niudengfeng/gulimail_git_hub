package com.atguigu.gulimail.search.vo;

import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.page.PageResult;
import lombok.Data;

import java.util.List;

@Data
public class SearchResultResponse extends PageResult {

    //查询到的所有商品信息
    private List<SkuEsModel> products;

    //查询到的商品所涉及到的所有品牌信息
    private List<BrandVo> brands;

    //查询到的商品所涉及到的所有属性信息
    private List<AttrVo> attrs;

    //查询到的商品所涉及到的所有属性信息
    private List<CategoryVo> categorys;

    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    //页码集合
    private List<Integer> navs;

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CategoryVo{
        private Long catalogId;
        private String catalogName;
    }

    //面包屑导航集合
    private List<NavVo> navos;

    @Data
    public static class NavVo{
        private Long attrId;
        private String attrName;
        private String attrValue;
        private String backUrl;//去除属性条件后应该回退到的链接URL
    }
}
