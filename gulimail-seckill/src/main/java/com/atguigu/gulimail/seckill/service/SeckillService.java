package com.atguigu.gulimail.seckill.service;

import com.atguigu.common.vo.SeckillSkuRelationRedisTo;

import java.util.List;

public interface SeckillService {

    List<SeckillSkuRelationRedisTo> getCurrentSkus();

    public SeckillSkuRelationRedisTo getSkuInfoBySkuId(Long skuId);

    String kill(String skuIdPromotionSessionId, String token, String count);

    public String normalResource(String code);
}
