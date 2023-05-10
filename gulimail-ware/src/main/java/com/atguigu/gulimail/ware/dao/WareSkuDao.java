package com.atguigu.gulimail.ware.dao;

import com.atguigu.gulimail.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:02:33
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateByWrapper(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Integer hasStockBySkuId(Long skuId);
}
