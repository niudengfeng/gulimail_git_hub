package com.atguigu.gulimail.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 一.引入thymeleaf
 *  1.        <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-thymeleaf</artifactId>
 *         </dependency>
 * 以下两步是为了更改代码可以实时看到效果
 *   2.开发期间关闭缓存，可以改变代码实时看到效果：spring.thymeleaf.cache=false
 *   3.引入dev-tools
 *         <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-devtools</artifactId>
 *         </dependency>
 *
 *  二：引入redisson
 *  1.引入依赖
 *  2.配置连接
 *  3.使用RedissonClient
 *
 *  REDIS分布式锁
 *  * 基于 SET NX
 *  * 1.可重入：利用hash结构记录线程ID和重入次数。
 *  * 2.可重试：利用信号量重入次数减到0和发布订阅机制实现等待唤醒获取锁失败的重试机制
 *  * 3.超时续约：利用watchDog(如果tryLock超时时间不填默认用30秒看门狗的时间，否则不会开启看门狗机制) 每隔三分之一 releaseTime 会重置超时时间
 *  * 4.主从一致性：如果redis集群部署的话，需要配置联锁。不然会导致分布式锁失效
 *  *
 *  *
 *  * 读写锁：
 *  *  * 读+读  相当于无锁
 *  *  * 读+写  写锁需要等待读锁释放
 *  *  * 写+读  读锁需要写锁释放
 *  *  * 写+写  阻塞时等待，第二个写锁需要等待第一个写锁释放
 *1.缓存穿透：
 * 简介：查一个不存在的值
 * 现象：大并发去查询一个缓存中不存在的值，这时压力给到数据库，相当于没有缓存功能了。
 * 解决：允许缓存NULL值，查不到也给缓存中把该key，给个Null值，并加个过期时间
 *
 * 2.缓存雪崩
 * 简介：大量key同时失效
 * 现象：我们定义缓存的失效时间相同，在同一时间大量key同时失效，导致数据库压力过大崩溃
 * 解决：过期时间加一个随机时间值
 *
 * 3.缓存击穿
 * 简介：热点key失效
 * 现象：热点key,大量并发请求去查这一个值，但是刚好失效，这时压力全部给到数据库
 * 解决：针对热点key进行加锁，大并发请求过来，只有一个线程获取到锁，查完重新缓存到redis，然后释放锁，其他人从缓存获取。
 *
 *
 * 三、引入spring-cache
 *  依赖  spring-cache
 *          <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-cache</artifactId>
 *         </dependency>
 *         redis
 *        <dependency>
 *             <groupId>org.springframework.boot</groupId>
 *             <artifactId>spring-boot-starter-data-redis</artifactId>
 *         </dependency>
 *
 *         1.配置redis作为缓存中间件spring.cache.type=redis
 *         2.测试使用缓存注解
             * @Cacheable :触发将数据保存到缓存中去的操作
             * @CacheEvict:触发将数据从缓存中删除的操作
             * @CachePut:不影响方法执行去更新缓存
             * @Caching:组合以上多个操作
             * @CacheConfig:在类级别共享缓存的相同配置
 *         3.在启动类上开启缓存注解 @EnableCaching
 *         4.原理:
 *              CacheAutoConfiguration -> RedisCacheConfiguration ->自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用什么配置
 *              ->如果redisCacheConfiguration有就用已有的，没有就用默认配置->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *              ->就会应用到当前RedisCacheManager管理的所有缓存分区中
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@MapperScan("com.atguigu.gulimail.product.dao")
public class GuilimailProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuilimailProductApplication.class, args);
    }

}
