package com.atguigu.gulimail.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");//允许所有请求方式跨域
        configuration.addAllowedOrigin("*");
        configuration.setAllowCredentials(true);//不会清空缓存
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**",configuration);//拦截所有请求
        CorsWebFilter corsWebFilter = new CorsWebFilter(urlBasedCorsConfigurationSource);
        return corsWebFilter;
    }

}
