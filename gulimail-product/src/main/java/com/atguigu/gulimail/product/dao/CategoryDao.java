package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
