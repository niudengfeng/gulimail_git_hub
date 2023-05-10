package com.atguigu.gulimail.product.service.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-coupon")
public interface FeignCouponService {

    @RequestMapping("/coupon/spubounds/save")
    R saveBound(@RequestBody Bounds bounds);

    @RequestMapping("/coupon/skuladder/save")
    R saveLadder(@RequestBody SkuLadderVo skuLadder);

    @RequestMapping("/coupon/skufullreduction/save")
    R saveFullReduction(@RequestBody SkuFullReductionVo skuFullReductionVo);

    @RequestMapping("/coupon/memberprice/saveBatch")
    R saveMemberPrice(@RequestBody List<MemberPriceVo> memberPriceVoList);
}
