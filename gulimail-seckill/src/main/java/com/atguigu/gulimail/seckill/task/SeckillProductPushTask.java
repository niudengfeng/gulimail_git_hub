package com.atguigu.gulimail.seckill.task;

import cn.hutool.core.date.DateUtil;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.gulimail.seckill.service.UpSeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 秒杀商品的定时上架
 *  1.每天凌晨三点执行任务：上架最近三天的秒杀商品
 *  当天（00：00:00-23:59:59）
 *  明天（00：00:00-23:59:59）
 *  后天（00：00:00-23:59:59）
 */
@Slf4j
@Component
@EnableScheduling
public class SeckillProductPushTask {

    @Autowired
    private UpSeckillProductService upSeckillProductService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * todo 幂等性：不能一个商品重复上架到redis
     */
    @Scheduled(cron = "0 0/1 * ? * *")
    public void upSeckillProduct(){
        log.info("开始跑批上架秒杀商品"+ DateUtil.now());
        //1.重复上架无需处理
        RLock lock = redissonClient.getLock(RedisConstants.SECKILL_LOCK);
        try {
            lock.lock();
            upSeckillProductService.upSeckillProduct();
        }catch (Exception e){
            log.error("跑批上架秒杀商品异常："+e);
        }finally {
            lock.unlock();
            log.info("结束跑批上架秒杀商品"+ DateUtil.now());
        }
    }
}
