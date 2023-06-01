package com.atguigu.gulimail.seckill.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 * 1、@EnableScheduling 开启定时务
 * 2、@Scheduled 开启一个定时任务
 * 3、自动配置类 TaskSchedulingAutoConfiguration
 * 水
 * *异步任务
 * 1、@EnableAsync 开启异步任务功能2、@Asvns 给希望异步执行的方法上标注3、自动配置类 TaskExecutionAutoConfiguration重性绑定在TaskExecutionProperties
 */

@Slf4j
@Component
@EnableAsync//表明异步类
@EnableScheduling
public class Test {

    /**
     * *1、spring中6位组成，不允许第7位的年
     * *2、在周几的位置，1-7代表周一到周日;MON-SUN
     * *3、定时任务不应该阻塞。默认是阻塞的
     *      1) 、可以让业务运行以异步的方式，自己提交到线程池
                 * CompletableFuture.runAsync(()->
                 * xxxxService.hello()1,executor);
     *      2)、支持定时任务线程池，设置 TaskSchedulingProperties;spring.task.scheduling.pool.size=5
     *      3)、让定时任务异步执行异步任务;
     *      解决:使用异步+定时任务来完成定时任务不阻塞的功能，
     */

    @Async//表示这个方法是异步执行的
    @Scheduled(cron = "*/5 * * ? * *")//cron :秒 分 时 日 月 周
    public void test(){

    }

}