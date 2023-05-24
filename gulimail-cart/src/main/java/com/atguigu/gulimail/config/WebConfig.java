package com.atguigu.gulimail.config;

import com.atguigu.gulimail.interpt.CartInterpt;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 拦截所有请求
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterpt()).addPathPatterns("/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
