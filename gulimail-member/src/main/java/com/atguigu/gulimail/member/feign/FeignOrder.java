package com.atguigu.gulimail.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Component
@FeignClient(value = "gulimail-order")
public interface FeignOrder {

    @PostMapping("/order/order/getOrderPage")
    public R getOrderPage(@RequestBody Map<String, Object> params);

}
