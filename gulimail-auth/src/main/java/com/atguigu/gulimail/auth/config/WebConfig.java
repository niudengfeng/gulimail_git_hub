package com.atguigu.gulimail.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加视图路径映射
     * 相当于下面这个controller里面的方法
     * @GetMapping("/login.html")
     *     public String login(){
     *         return "index";
     *     }
     * @param registry
     */
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("index");
        registry.addViewController("/reg.html").setViewName("reg");
    }

}
