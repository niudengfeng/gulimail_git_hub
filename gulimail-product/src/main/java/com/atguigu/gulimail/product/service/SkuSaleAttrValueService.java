package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.SkuItemVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:18
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemVo.SpuSalesVo> listSalesVo(Long spuId);
}

