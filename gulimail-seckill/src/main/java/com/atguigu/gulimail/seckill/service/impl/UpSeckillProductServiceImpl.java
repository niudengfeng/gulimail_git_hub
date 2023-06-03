package com.atguigu.gulimail.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.SeckillSessionRedisTo;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.common.vo.SkuInfoTo;
import com.atguigu.gulimail.seckill.entity.SeckillSessionEntity;
import com.atguigu.gulimail.seckill.service.UpSeckillProductService;
import com.atguigu.gulimail.seckill.service.feign.CouponFeign;
import com.atguigu.gulimail.seckill.service.feign.ProductFeign;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UpSeckillProductServiceImpl implements UpSeckillProductService {

    @Autowired
    private CouponFeign couponFeign;

    @Autowired
    private ProductFeign productFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void upSeckillProduct() {

        //1.扫描需要上架的秒杀商品，远程调用
        R r = couponFeign.getSeckillProductCurrentThreeDays();
        if (r.getCode() == 0){
            List<SeckillSessionRedisTo> data = (List<SeckillSessionRedisTo>) JSON.parseObject(JSON.toJSONString(r.get("data")), ArrayList.class);
            if (CollectionUtils.isEmpty(data)){
                return;
            }
            //2.保存活动ID信息到REDIS
            saveSeckillSessionIds(data);
            //3.保存商品信息和秒杀活动基本信息到REDIS
            saveSkuInfo(data);
        }
    }

    private void saveSeckillSessionIds(List<SeckillSessionRedisTo> data){
        for (Object datum : data) {
            if (datum!=null){
                SeckillSessionRedisTo seckillSessionRedisTo = JSON.parseObject(JSON.toJSONString(datum), SeckillSessionRedisTo.class);
                String key = RedisConstants.SECKILL_RELATION_ID_KEY+seckillSessionRedisTo.getStartTime().getTime()+"-"+seckillSessionRedisTo.getEndTime().getTime();
                if (! redisTemplate.hasKey(key)){//先检查这个KEY是否已经存在了。防止重复入库
                    List<String> collect = seckillSessionRedisTo.getRelationRedisTos().stream().map(d -> d.getPromotionSessionId()+"_"+d.getSkuId().toString()).collect(Collectors.toList());
                    //场次ID—SKUID：设置过期时间
                    long endTime = seckillSessionRedisTo.getEndTime().getTime();
                    long curr = new Date().getTime();
                    long ttl = endTime-curr;
                    redisTemplate.opsForList().leftPushAll(key,collect);
                    redisTemplate.opsForList().getOperations().expire(key,ttl,TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void saveSkuInfo(List<SeckillSessionRedisTo> data){
        //循环遍历所有符合的场次
        for (Object d : data) {
            if (d!=null){
                BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(RedisConstants.SECKILL_RELATION_INFO_KEY);
                //循环遍历每个场次锁关联的商品
                SeckillSessionRedisTo datum = JSON.parseObject(JSON.toJSONString(d), SeckillSessionRedisTo.class);
                datum.getRelationRedisTos().stream().forEach(m->{
                    Long skuId = m.getSkuId();
                    R r = productFeign.getSkuInfo(skuId);
                    if (r.getCode() == 0){
                        Object o = r.get("skuInfo");
                        if (o!=null){
                            SkuInfoTo skuInfoTo = JSON.parseObject(JSON.toJSONString(o), SkuInfoTo.class);
                            m.setSkuInfoTo(skuInfoTo);//商品信息
                            m.setStartTime(datum.getStartTime().getTime());
                            m.setEndTime(datum.getEndTime().getTime());
                            //4.给每个商品分配个随机码,
                            m.setToken(UUID.randomUUID().toString().replace("-",""));
                            String hk = m.getPromotionSessionId() + "_" + skuId.toString();
                            //先判断当前场次对应的商品是否已经上架过了
                            if (!hashOps.hasKey(hk)){
                                long endTime = datum.getEndTime().getTime();
                                long curr = new Date().getTime();
                                long ttl = endTime-curr;
                                hashOps.expire(ttl,TimeUnit.MILLISECONDS);
                                hashOps.put(hk,JSON.toJSONString(m));
                                //5.引入分布式信号量，为秒杀限流。
                                //5.1 每个商品的token作为信号量的key
                                String key = RedisConstants.SECKILL_SEMAPHORE_KEY+ m.getToken();
                                RSemaphore semaphore = redissonClient.getSemaphore(key);
                                //5.2每个上架的秒杀商品对应总数量作为信号量限流量 取消订单释放，创建订单占用
                                semaphore.trySetPermits(m.getSeckillCount().intValue());
                                //设置信号量的过期时间
                                semaphore.expire(ttl,TimeUnit.MILLISECONDS);
                            }
                        }
                    }
                });
            }
        }
    }
}
