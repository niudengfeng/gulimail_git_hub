package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.ProductAttrValueDao;
import com.atguigu.gulimail.product.entity.SkuImagesEntity;
import com.atguigu.gulimail.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimail.product.service.SkuImagesService;
import com.atguigu.gulimail.product.service.SkuSaleAttrValueService;
import com.atguigu.gulimail.product.service.SpuInfoDescService;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.atguigu.gulimail.product.vo.SpuAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.SkuInfoDao;
import com.atguigu.gulimail.product.entity.SkuInfoEntity;
import com.atguigu.gulimail.product.service.SkuInfoService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private SpuInfoDescService descService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        BigDecimal min = new BigDecimal(params.get("min")==null?"0":String.valueOf(params.get("min")));
        if (min!=null && min.compareTo(BigDecimal.ZERO)!=0){
            wrapper.ge("price",min);
        }
        BigDecimal max = new BigDecimal(params.get("max")==null?"0":String.valueOf(params.get("max")));
        if (max!=null && max.compareTo(BigDecimal.ZERO)!=0){
            wrapper.le("price",max);
        }
        String brandId = String.valueOf(params.get("brandId")==null?"0":params.get("brandId"));
        if (!StringUtils.isEmpty(brandId) && Long.parseLong(brandId)!=0L){
            wrapper.eq("brand_id",Long.parseLong(brandId));
        }
        String catelogId = String.valueOf(params.get("catelogId")==null?"0":params.get("catelogId"));
        if (!StringUtils.isEmpty(catelogId) && Long.parseLong(catelogId)!=0L){
            wrapper.eq("catalog_id",Long.parseLong(catelogId));
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> skus = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skus;
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo vo = new SkuItemVo();
        //1、sku基本信息获取 pms_sku_info
        SkuInfoEntity skuInfo = this.getById(skuId);
        vo.setSkuInfo(skuInfo);
        //2、sku的图片信息pms_sku_images
        QueryWrapper<SkuImagesEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id",skuId);
        List<SkuImagesEntity> images = imagesService.getBaseMapper().selectList(queryWrapper);
        vo.setImages(images);
        //3、获取spu的销售属性组合。
        List<SkuItemVo.SpuSalesVo> salesVos = skuSaleAttrValueService.listSalesVo(skuInfo.getSpuId());
        vo.setSales(salesVos);
        //4、获取spu的介绍
        SpuInfoDescEntity descEntity = descService.getById(skuInfo.getSpuId());
        vo.setDesc(descEntity.getDecript());
        //5、获取spu的规格参数信息
        List<SkuItemVo.SpuAttrGroupVo> attrGroupVos = new ArrayList<>();
        List<SpuAttrVo> attrVos = productAttrValueDao.attrGroupVos(skuInfo.getSpuId());
        if (! CollectionUtils.isEmpty(attrVos)){
            for (Map.Entry<String, List<SpuAttrVo>> m : attrVos.stream().collect(groupingBy(SpuAttrVo::getAttrGroupName))
                    .entrySet()) {
                String groupName = m.getKey();
                List<SpuAttrVo> value = m.getValue();
                SkuItemVo.SpuAttrGroupVo spuAttrGroupVo = new SkuItemVo.SpuAttrGroupVo();
                spuAttrGroupVo.setGroupName(groupName);
                spuAttrGroupVo.setAttrBaseVos(value);
                attrGroupVos.add(spuAttrGroupVo);
            }
        }
        vo.setSpuAttrGroupVos(attrGroupVos);
        return vo;
    }
}
