package com.atguigu.gulimail.seckill.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.constants.MqConstants;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.common.vo.SkuInfoTo;
import com.atguigu.common.vo.seckill.SeckillOrderVo;
import com.atguigu.gulimail.seckill.interceptor.LoginUserInterCeptor;
import com.atguigu.gulimail.seckill.service.SeckillService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    private RabbitTemplate rabbitTemplate;

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
                    if (time>endTime){
                        //说明活动结束了
                        continue;
                    }
                    if (time<startTime){
                        //活动还未开始呢，token隐藏
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

    /**
     *
     * @param skuIdPromotionSessionId 1_32
     * @param token
     * @param count
     */
    @Override
    public String kill(String skuIdPromotionSessionId, String token, String count) {
        MemberVO memberVO = LoginUserInterCeptor.threadLocal.get();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(RedisConstants.SECKILL_RELATION_INFO_KEY);
        Object o = hashOps.get(skuIdPromotionSessionId);
        //1.合法性校验
        boolean validate = validate(skuIdPromotionSessionId, token, count,memberVO.getId(),o);
        if (validate){
            SeckillSkuRelationRedisTo seckillSkuRelationRedisTo = JSON.parseObject(o.toString(), SeckillSkuRelationRedisTo.class);
            //2.校验通过，信号量占位
            String key = RedisConstants.SECKILL_SEMAPHORE_KEY+ token;
            RSemaphore semaphore = redissonClient.getSemaphore(key);
            boolean b = semaphore.tryAcquire(Integer.valueOf(count));
            if (b){
                log.info("恭喜。秒杀成功");
                String orderSn = IdWorker.getTimeId();
                //这里为了快速处理，直接发个MQ消息，
                SeckillOrderVo seckillOrderVo = new SeckillOrderVo();
                seckillOrderVo.setSeckillPrice(seckillSkuRelationRedisTo.getSeckillPrice());
                seckillOrderVo.setOrderSn(orderSn);
                seckillOrderVo.setCount(Integer.valueOf(count));
                seckillOrderVo.setSkuId(seckillSkuRelationRedisTo.getSkuId());
                seckillOrderVo.setMemberId(memberVO.getId());
                seckillOrderVo.setProsessionId(seckillSkuRelationRedisTo.getPromotionSessionId());
                rabbitTemplate.convertAndSend(MqConstants.seckillEventExchange,MqConstants.seckillRoutingKey
                        ,seckillOrderVo,new CorrelationData(orderSn));
                return orderSn;
            }
        }
        return null;
    }


    private boolean validate(String skuIdPromotionSessionId, String token, String count,Long id,Object o){
        if (o==null){
            log.error("无此场次的秒杀商品");
            return false;
        }
        //1.校验时间范围
        SeckillSkuRelationRedisTo seckillSkuRelationRedisTo = JSON.parseObject(o.toString(), SeckillSkuRelationRedisTo.class);
        long now = new Date().getTime();
        long startTime = seckillSkuRelationRedisTo.getStartTime();
        long endTime = seckillSkuRelationRedisTo.getEndTime();
        if (now<startTime || now>endTime){
            log.error("此场次的秒杀商品已结束");
            return false;
        }
        //2.校验token和场次
        String skuId = seckillSkuRelationRedisTo.getSkuId().toString();
        String promotionSessionId = seckillSkuRelationRedisTo.getPromotionSessionId().toString();
        String key = promotionSessionId+"_"+skuId;
        if (!(skuIdPromotionSessionId.equals(key) && token.equals(seckillSkuRelationRedisTo.getToken()))){
            log.error("token核验失败！");
            return false;
        }
        //3.校验购物数量
        if (StringUtils.isEmpty(count)
                || Integer.valueOf(count).intValue()>seckillSkuRelationRedisTo.getSeckillLimit().intValue()
                || Integer.valueOf(count).intValue()>seckillSkuRelationRedisTo.getSeckillSort().intValue()){
            log.error("购买数量不合法！");
            return false;
        }
        //4.验证这个人是否已经购买过了 seckill:shopped:prosessionId:skuId:userId
        String useKey = "seckill:shopped:"+key+":"+id.toString();
        long ttl = endTime-now;//活动结束时间-当前购买时间就是这个占位KEY的ttl
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(useKey, count, ttl, TimeUnit.MILLISECONDS);
        if (!aBoolean){
            //占坑失败，购买过了
            log.error("您已经购买过了！");
            return false;
        }
        return true;
    }


    /**
     * 可以指定异常回调方法，但是必须在本方法类并且参数名 返回值类型需要一样
     * @param code
     * @return
     */
    @SentinelResource(value = "normalResources",blockHandler = "normalResourceHandler")
    public String normalResource(String code){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return code + "===>normal";
    }

    public String normalResourceHandler(String code, BlockException exception){
        return code + "===>error";
    }
}
