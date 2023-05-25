package com.atguigu.gulimail.order.feign;

import com.atguigu.gulimail.order.vo.CartItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "gulimail-cart")
public interface CartFeign {

    @GetMapping("/getCartsByMemberId")
    List<CartItem> getCartsByMemberId();

}
