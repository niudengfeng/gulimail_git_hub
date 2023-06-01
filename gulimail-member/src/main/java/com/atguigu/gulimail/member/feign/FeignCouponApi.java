package com.atguigu.gulimail.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

@Component
@FeignClient(value = "gulimail-coupon")
public interface FeignCouponApi {

    @RequestMapping("/coupon/nacos/test")
    public R getNacosConfigInfo();

}
