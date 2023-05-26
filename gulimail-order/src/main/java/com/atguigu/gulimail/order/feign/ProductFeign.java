package com.atguigu.gulimail.order.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-product")
public interface ProductFeign {

    @RequestMapping("/product/spuinfo/getInfoBySkuId/{skuId}")
    public R getInfoBySkuId(@PathVariable("skuId") Long skuId);
}
