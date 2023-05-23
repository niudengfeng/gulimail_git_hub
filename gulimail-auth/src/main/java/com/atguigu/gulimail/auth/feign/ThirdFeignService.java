package com.atguigu.gulimail.auth.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimail-third")
public interface ThirdFeignService {
    @RequestMapping(value = "/sms/sendCode",method = RequestMethod.GET)
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code);
}
