package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.entity.IndexCatelogGoryVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithChildren();

    void removeBatchByIds(List<Long> catIds);

    void updateByIdAndRelation(CategoryEntity category);

    List<CategoryEntity> categoryLevelOne();

    Map<Long,List<IndexCatelogGoryVo>> getCateGory3FromDb(List<CategoryEntity> ones);

    Map categoryLevelThree();
}

