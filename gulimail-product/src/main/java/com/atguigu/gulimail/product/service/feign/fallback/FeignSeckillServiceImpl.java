package com.atguigu.gulimail.product.service.feign.fallback;

import com.atguigu.common.BizCode;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.service.feign.FeignSeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignSeckillServiceImpl implements FeignSeckillService {
    @Override
    public R getSkuInfo(Long skuId) {
        log.error("记录远程调用秒杀服务异常：getSkuInfo检查当前商品是否有秒杀活动skuId："+skuId);
        return R.error(BizCode.FEIGN_FAIL.getCode(),BizCode.FEIGN_FAIL.getMsg());
    }
}
