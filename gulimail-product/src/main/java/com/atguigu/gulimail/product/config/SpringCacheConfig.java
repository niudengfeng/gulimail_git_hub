package com.atguigu.gulimail.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 开启spring cache缓存
 */
@Configuration
@EnableCaching
public class SpringCacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();//读取缓存配置属性
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();//使用默认配置，在此基础上修改
        //设置kv的序列化
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));//string  key
        //设置value是json序列化
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));

        //设置ttl过期时间
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        //设置前缀
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        //设置允许缓存空值，防止缓存穿透
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        //是否允许使用前缀
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

}
