package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.entity.BrandEntity;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.BrandService;
import com.atguigu.gulimail.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimail.product.service.CategoryBrandRelationService;

import javax.annotation.Resource;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Resource
    BrandService brandService;

    @Resource
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<>();
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> queryPageByBrandId(Long brandId) {
        QueryWrapper<CategoryBrandRelationEntity> wrapper = new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId);
       return this.baseMapper.selectList(wrapper);
    }

    @Override
    public void saveAndGetRelationInfo(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandService.getById(brandId);
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        String brandName = "";
        String cateName = "";
        if (brandEntity!=null){
            brandName = brandEntity.getName();
            categoryBrandRelation.setBrandName(brandName);
        }
        if (categoryEntity!=null){
            cateName = categoryEntity.getName();
            categoryBrandRelation.setCatelogName(cateName);
        }
        this.save(categoryBrandRelation);
    }

    @Override
    public List<CategoryBrandRelationEntity> getBrandListByCatId(Long catId) {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("catelog_id",catId);
        return this.listByMap(columnMap);
    }

}