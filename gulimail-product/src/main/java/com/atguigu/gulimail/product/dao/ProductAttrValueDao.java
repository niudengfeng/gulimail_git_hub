package com.atguigu.gulimail.product.dao;

import com.atguigu.gulimail.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimail.product.vo.SpuAttrVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * spu属性值
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:18
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<SpuAttrVo> attrGroupVos(@Param("spuId") Long spuId);

}
