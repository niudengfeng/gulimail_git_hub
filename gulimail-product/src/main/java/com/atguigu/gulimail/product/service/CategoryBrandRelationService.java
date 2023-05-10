package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryBrandRelationEntity> queryPageByBrandId(Long brandId);

    void saveAndGetRelationInfo(CategoryBrandRelationEntity categoryBrandRelation);

    List<CategoryBrandRelationEntity> getBrandListByCatId(Long catId);
}

