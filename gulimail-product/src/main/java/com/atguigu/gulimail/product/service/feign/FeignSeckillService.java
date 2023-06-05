package com.atguigu.gulimail.product.service.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.service.feign.fallback.FeignSeckillServiceImpl;
import com.atguigu.gulimail.product.vo.Bounds;
import com.atguigu.gulimail.product.vo.MemberPriceVo;
import com.atguigu.gulimail.product.vo.SkuFullReductionVo;
import com.atguigu.gulimail.product.vo.SkuLadderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-seckill",fallback = FeignSeckillServiceImpl.class)
public interface FeignSeckillService {

    @RequestMapping("/seckill/getSkuInfo/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);

}
