package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
    List<Long> getSearchDataByAttrIds(@Param("attrIds") List<Long> attrIds);
}
