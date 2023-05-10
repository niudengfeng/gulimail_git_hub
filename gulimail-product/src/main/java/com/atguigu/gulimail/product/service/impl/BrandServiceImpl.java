package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.CategoryBrandRelationDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.BrandDao;
import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.service.BrandService;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (key!=null){
            String s = key.toString();
            if (StringUtils.isNotEmpty(s)){
                //包含参数封装条件
                wrapper.eq("brand_id",s).or().like("name",s);
            }
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void updateByIdAndRelation(BrandEntity brand) {
        this.updateById(brand);
        if (StringUtils.isNotEmpty(brand.getName())){
            Map map = new HashMap();
            map.put("brandId",brand.getBrandId());
            map.put("brandName",brand.getName());
            categoryBrandRelationDao.updateByBrandId(map);
        }
    }

}