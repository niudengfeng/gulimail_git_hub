package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.SaveSpuInfoBean;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:18
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveInfo(SaveSpuInfoBean spuInfoBean);

    void up(Long spuId);

    SpuInfoEntity getInfoBySkuId(Long skuId);
}

