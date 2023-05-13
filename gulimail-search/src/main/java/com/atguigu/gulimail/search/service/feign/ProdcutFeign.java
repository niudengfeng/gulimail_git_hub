package com.atguigu.gulimail.search.service.feign;


import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "gulimail-product")
public interface ProdcutFeign {

    @RequestMapping("/product/attr/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId);

}
