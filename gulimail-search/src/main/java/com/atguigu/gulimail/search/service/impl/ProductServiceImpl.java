package com.atguigu.gulimail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.EsConstants;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.search.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public R saveSkus(List<SkuEsModel> skuEsModels) throws IOException {
        log.info("开始执行ES服务skuEsModels");
        if (skuEsModels == null || skuEsModels.size()<=0){
            log.info("skuEsModels为空");
            return R.error();
        }
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest request = new IndexRequest(EsConstants.PRODUCT_INDEX);
            request.id(skuEsModel.getSkuId().toString());
            request.source(JSON.toJSONString(skuEsModel),XContentType.JSON);
            bulkRequest.add(request);
        }
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        log.info("结束执行ES服务:response:{}",response);
        if (response.hasFailures()){
            log.error("错误信息：{}",response.getItems().toString());
            return R.error("执行ES插入失败");
        }else {
            return R.ok();
        }
    }
}
