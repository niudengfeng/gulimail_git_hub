package com.atguigu.gulimail.search.service;


import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.R;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    R saveSkus(List<SkuEsModel> skuEsModels) throws IOException;

}
