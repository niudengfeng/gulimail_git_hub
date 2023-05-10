package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 品牌分类关联
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateByBrandId(Map map);
    void updateByCatelogId(Map map);
}
