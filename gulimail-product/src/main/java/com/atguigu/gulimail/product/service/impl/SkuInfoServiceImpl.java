package com.atguigu.gulimail.product.service.impl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
}