package com.atguigu.gulimail.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "my.thread")
public class ThreadPoolProperties {
    private int corePoolSize;//核心线程数
    private int maximumPoolSize;//最大线程数
    private long keepAliveTime;//当线程数大于核心线程数的时候，非核心线程在最大多长时间没有接到新任务就会终止释放，最终线程池维持在 corePoolSize 大小
    private int queueMax;//队列大小
}
