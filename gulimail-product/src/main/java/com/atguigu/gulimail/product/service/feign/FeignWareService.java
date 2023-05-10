package com.atguigu.gulimail.product.service.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-ware")
public interface FeignWareService {

    @RequestMapping("ware/waresku/hasStock")
    R hasStock(@RequestBody List<Long> skuIds);

}
