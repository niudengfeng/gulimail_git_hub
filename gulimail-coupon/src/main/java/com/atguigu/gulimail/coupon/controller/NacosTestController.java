//package com.atguigu.gulimail.coupon.controller;
//
//import com.atguigu.common.utils.R;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RefreshScope
//@RequestMapping("coupon/")
//public class NacosTestController {
//
//    @Value("${coupon.user.name}")
//    private String username;
//
//    @Value("${coupon.user.age}")
//    private Integer age;
//
//    @RequestMapping("nacos/test")
//    public R getNacosConfigInfo(){
//        return R.ok().put("username",username).put("age",age);
//    }
//
//}
