package com.atguigu.gulimail.product.vo;

import com.atguigu.gulimail.product.entity.SkuImagesEntity;
import com.atguigu.gulimail.product.entity.SkuInfoEntity;
import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    //1、sku基本信息获取 pms_sku_info
    private SkuInfoEntity skuInfo;
    //2、sku的图片信息pms_sku_images
    private List<SkuImagesEntity> images;
    //3、获取spu的销售属性组合。
    private List<SpuSalesVo> sales;
    //4、获取spu的介绍
    private String desc;
    //5、获取spu的规格参数信息
    private List<SpuAttrGroupVo> spuAttrGroupVos;
    //6.是否有货
    private boolean hasStock = true;

    @Data
    public static class SpuAttrGroupVo{
        private String groupName;
        private List<SpuAttrVo> attrBaseVos;
    }
}
