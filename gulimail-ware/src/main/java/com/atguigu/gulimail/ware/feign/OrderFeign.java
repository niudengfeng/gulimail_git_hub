package com.atguigu.gulimail.ware.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimail-order")
public interface OrderFeign {
    @RequestMapping("/order/order/getOrderStatus/{orderSn}")
    public R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
