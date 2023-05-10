package com.atguigu.gulimail.search.controller;

import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.search.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("search")
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping("/saveSkus")
    public R saveSkus(@RequestBody List<SkuEsModel> skuEsModels) throws IOException {
        return productService.saveSkus(skuEsModels);
    }

}
