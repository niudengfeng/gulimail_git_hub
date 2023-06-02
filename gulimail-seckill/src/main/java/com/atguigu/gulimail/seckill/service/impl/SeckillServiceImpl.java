package com.atguigu.gulimail.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.common.vo.SkuInfoTo;
import com.atguigu.gulimail.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<SeckillSkuRelationRedisTo> getCurrentSkus() {
        List<SeckillSkuRelationRedisTo> skus = new ArrayList<>();
        //1.先从redis获取符合当期时间的场次
        long time = new Date().getTime();//当前系统时间
        String seckillRelationIdKey = RedisConstants.SECKILL_RELATION_ID_KEY;//场次key
        Set<String> keys = redisTemplate.keys(seckillRelationIdKey+"*");
        if (!CollectionUtils.isEmpty(keys))  {
            for (String key : keys) {
                String s = key.split("-")[0];
                long start = Long.parseLong(s.substring(s.lastIndexOf(":")+1));
                long end = Long.parseLong(key.split("-")[1]);
                if (time>=start && time<=end){
                    //符合时间段,取出这个key对应的数据这里传入-100 到 100  一般一个场次不会关联超过100个商品的
                    List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                    //2.根据当前场次获取到所有商品信息 range:就是{“1-1”，“1-2”} 前面是场次id，后面是skuId
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(RedisConstants.SECKILL_RELATION_INFO_KEY);
                    List<String> strings = hashOps.multiGet(range);
                    if (!CollectionUtils.isEmpty(strings)){
                        List<SeckillSkuRelationRedisTo> collect = strings.stream().map(m -> {
                            SeckillSkuRelationRedisTo skuInfoTo = JSON.parseObject(m, SeckillSkuRelationRedisTo.class);
                            skuInfoTo.setSeckillPrice(skuInfoTo.getSeckillPrice().setScale(2, RoundingMode.HALF_UP));
                            SkuInfoTo skuInfoTo1 = skuInfoTo.getSkuInfoTo();
                            skuInfoTo1.setPrice(skuInfoTo1.getPrice().setScale(2,RoundingMode.HALF_UP));
                            return skuInfoTo;
                        }).collect(Collectors.toList());
                        skus.addAll(collect);
                    }
                }
            }
        }
        return skus;
    }

    public SeckillSkuRelationRedisTo getSkuInfoBySkuId(Long skuId) {
        List<SeckillSkuRelationRedisTo> list = new ArrayList<>();
        //1.获取当前skuId所有的key
        BoundHashOperations<String, String, String> has = redisTemplate.boundHashOps(RedisConstants.SECKILL_RELATION_INFO_KEY);
        Set<String> keys = has.keys();
        if (!CollectionUtils.isEmpty(keys)){
            String regx = "\\d_"+skuId;
            for (String key : keys) {
                if (Pattern.matches(regx,key)) {
                    String json = has.get(key);
                    SeckillSkuRelationRedisTo to = JSON.parseObject(json, SeckillSkuRelationRedisTo.class);
                    //如果当前商品秒杀时间没到需要token隐藏
                    long time = new Date().getTime();
                    long startTime = to.getStartTime();
                    long endTime = to.getEndTime();
                    if (time<startTime || time>endTime){
                        to.setToken(null);
                    }
                    list.add(to);
                }
            }
        }
        if (CollectionUtils.isEmpty(list)){
            return null;
        }
        //2.找到符合时间段的
        Collections.sort(list,Comparator.nullsLast(Comparator.comparing(SeckillSkuRelationRedisTo::getStartTime)));
        return list.get(0);
    }
}
