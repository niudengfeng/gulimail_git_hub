package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimail.product.vo.SkuItemVo;
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

    @Select("SELECT pv.attr_id as attrId,pv.attr_name as attrName,GROUP_CONCAT(DISTINCT pv.attr_value ) as attrValue \n" +
            "FROM `pms_sku_sale_attr_value` pv\n" +
            "WHERE pv.sku_id in (\n" +
            "SELECT pi.sku_id from pms_sku_info pi WHERE pi.spu_id=#{spuId}\n" +
            ")\n" +
            "GROUP BY pv.attr_id,pv.attr_name")
    List<SkuItemVo.SpuSalesVo> listSalesVo(@Param("spuId") Long spuId);
}
