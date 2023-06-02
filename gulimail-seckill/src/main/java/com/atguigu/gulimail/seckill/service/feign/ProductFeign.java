package com.atguigu.gulimail.seckill.service.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimail-product")
public interface ProductFeign {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);
}
