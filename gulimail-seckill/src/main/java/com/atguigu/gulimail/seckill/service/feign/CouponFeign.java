package com.atguigu.gulimail.seckill.service.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "gulimail-coupon")
public interface CouponFeign {

    @GetMapping("/coupon/seckillsession/getSeckillProductCurrentThreeDays")
    R getSeckillProductCurrentThreeDays();


}
