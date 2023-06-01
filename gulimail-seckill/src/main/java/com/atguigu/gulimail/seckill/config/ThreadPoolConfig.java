package com.atguigu.gulimail.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    /**
     * 自定义线程池
     * 运行流程：
     * 1、线程池创建，准备好 core 数量的核心线程，准备接受任务
     * 2、新的任务进来，用 core 准备好的空闲线程执行。
     * (1) 、core 满了，就将再进来的任务放入阻塞队列中。空闲的 core 就会自己去阻塞队
     * 列获取任务执行
     * (2) 、阻塞队列满了，就直接开新线程执行，最大只能开到 max 指定的数量
     * (3) 、max 都执行好了。Max-core 数量空闲的线程会在 keepAliveTime 指定的时间后自
     * 动销毁。最终保持到 core 大小
     * (4) 、如果线程数开到了 max 的数量，还有新任务进来，就会使用 reject 指定的拒绝策
     * 略进行处理
     * 3、所有的线程创建都是由指定的 factory 创建的
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties properties){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaximumPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100*1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        return threadPoolExecutor;
    }

}
