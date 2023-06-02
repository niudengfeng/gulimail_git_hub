package com.atguigu.gulimail.product.controller;

import com.atguigu.common.utils.R;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 读+读  相当于无锁
 * 读+写  写锁需要等待读锁释放
 * 写+读  读锁需要写锁释放
 * 写+写  阻塞时等待，第二个写锁需要等待第一个写锁释放
 */
@RestController
public class TestRedissonLock {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/lock")
    public R lock(){
        RLock lock = redissonClient.getLock("my-lock");
        try {
            lock.lock();//加锁
            System.out.println("执行业务逻辑");
        }finally {
            lock.unlock();//释放锁
        }
        return R.ok();
    }


    @GetMapping("/write")
    public R write(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("my-rw-lock");
        RLock rLock = readWriteLock.writeLock();//改数据加写锁
        try {
            rLock.lock();
            System.out.println("加写锁，执行业务逻辑...");
            String s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("write",s);
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();//释放锁
        }
        return R.ok();
    }



    @GetMapping("/read")
    public R read(){
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("my-rw-lock");
        RLock rLock = readWriteLock.readLock();//读数据加读锁
        String s = "";
        try {
            rLock.lock();//加锁
            System.out.println("加读锁，执行业务逻辑...");
            s = redisTemplate.opsForValue().get("write");
//            Thread.sleep(10*1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();//释放锁
        }
        return R.ok().put("s",s);
    }


    /**
     * 模拟 闭锁  锁门 比如五个同学放学，走一个减一个，全部走完 就锁门
     * @return
     */
    @GetMapping("/lockDoor")
    public String lockDoor(){
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLock");
        try {
            countDownLatch.trySetCount(5);//设置同学数量，
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        return "都走完了，锁门";
    }

    /**
     * 走一个，计数减一个
     * @param id
     * @return
     */
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id") Long id){
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLock");
        try {
            countDownLatch.countDown();//计数减一
        } catch (Exception e) {
        }
        return "第"+id+"个同学走了！";
    }


    /**
     * 信号量-释放
     * 模拟停车位
     * 车开走，释放一个停车位
     * @return
     */
    @GetMapping("/go")
    public String go(){
        RSemaphore semaphoreLock = redissonClient.getSemaphore("SemaphoreLock");
        semaphoreLock.trySetPermits(10);//假如设置10个车位
        try {
            semaphoreLock.release();
        } catch (Exception e) {
        }
        return "go";
    }

    /**
     * 信号量-占位
     * 也可以作为分布式限流方案
     * 模拟停车
     * @return
     */
    @GetMapping("/park")
    public String park(){
        RSemaphore semaphoreLock = redissonClient.getSemaphore("SemaphoreLock");
        boolean b = false;
        try {
//            semaphoreLock.acquire();//占车位，会一直等待
            b = semaphoreLock.tryAcquire();//尝试获取车位，如果获取到就走业务逻辑，否则返回业务繁忙等提示  不会等待
        } catch (Exception e) {
        }
        return "ok=》"+b;
    }
}
