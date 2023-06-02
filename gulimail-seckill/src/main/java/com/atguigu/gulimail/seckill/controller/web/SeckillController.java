package com.atguigu.gulimail.seckill.controller.web;

import com.atguigu.common.utils.R;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.gulimail.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/seckill")
@RestController
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/getCurrentSkus")
    public R getCurrentSkus(Model model){
        List<SeckillSkuRelationRedisTo> skus = seckillService.getCurrentSkus();
        model.addAttribute("skus",skus);
        return R.ok().put("skus",skus);
    }

    @GetMapping("/getSkuInfo/{skuId}")
    public R getSkuInfo(@PathVariable("skuId") Long skuId){
        SeckillSkuRelationRedisTo sku = seckillService.getSkuInfoBySkuId(skuId);
        return R.ok().put("sku",sku);
    }
}
