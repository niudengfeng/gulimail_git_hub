package com.atguigu.gulimail.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 基于 SET NX
 * 1.可重入：利用hash结构记录线程ID和重入次数。
 * 2.可重试：利用信号量重入次数减到0和发布订阅机制实现等待唤醒获取锁失败的重试机制
 * 3.超时续约：利用watchDog(如果tryLock超时时间不填默认用30秒看门狗的时间，否则不会开启看门狗机制) 每隔三分之一 releaseTime 会重置超时时间
 * 4.主从一致性：如果redis集群部署的话，需要配置联锁。不然会导致分布式锁失效
 *
 *
 * 读写锁：
 *  * 读+读  相当于无锁
 *  * 读+写  写锁需要等待读锁释放
 *  * 写+读  读锁需要写锁释放
 *  * 写+写  阻塞时等待，第二个写锁需要等待第一个写锁释放
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String adress;


    @Value("${spring.redis.port}")
    private int port;


    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private String database;


    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //单机版
        config.useSingleServer()
                .setAddress("redis://"+adress+":"+port)
                .setDatabase(Integer.valueOf(database))
                .setPassword(password);
        return Redisson.create(config);
    }
}
