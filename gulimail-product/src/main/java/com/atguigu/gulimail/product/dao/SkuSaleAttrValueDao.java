package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.atguigu.gulimail.product.vo.SpuSalesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * sku销售属性&值
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:18
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SpuSalesVo> listSalesVo(@Param("spuId") Long spuId);
}
