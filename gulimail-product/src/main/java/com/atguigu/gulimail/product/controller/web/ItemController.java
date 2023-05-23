package com.atguigu.gulimail.product.controller.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import com.atguigu.gulimail.product.service.SkuInfoService;
import com.atguigu.gulimail.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping(value = {"/{skuId}.html","item"})
    public String index(Model model,@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("skuItemVo", skuItemVo);
        return "item";
    }


}
