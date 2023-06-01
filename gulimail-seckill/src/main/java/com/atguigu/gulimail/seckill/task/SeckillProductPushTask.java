package com.atguigu.gulimail.seckill.task;

import com.atguigu.gulimail.seckill.service.UpSeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SeckillProductPushTask {

    @Autowired
    private UpSeckillProductService upSeckillProductService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void upSeckillProduct(){
        //1.重复上架无需处理
        upSeckillProductService.upSeckillProduct();
    }

}
