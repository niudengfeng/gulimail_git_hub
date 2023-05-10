package com.atguigu.gulimail.product.service.feign;

import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-search")
public interface FeignElasticSearchService {

    @RequestMapping("/search/saveSkus")
    R saveSkus(@RequestBody List<SkuEsModel> skuEsModels);

}
