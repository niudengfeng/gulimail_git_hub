package com.atguigu.gulimail.member.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.member.service.FeignCouponApi;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/member")
public class OpenFeignController {

    @Resource
    private FeignCouponApi feignCouponApil;

    @RequestMapping("/feign/test")
    public R getInfoByFeign(){
        return feignCouponApil.getNacosConfigInfo();
    }

}
