package com.atguigu.gulimail.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.host_1}")
    private String host1;

    @Value("${elasticsearch.port_1}")
    private Integer port1;

    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host1, port1, "http")
//                        ,new HttpHost("localhost", 9201, "http")//集群可以配置多个
                ));
        return client;
    }

    @Bean
    public RequestOptions setRequestOptions(){
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        /**builder.addHeader("Authorization", "Bearer " + TOKEN);
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));*/
        RequestOptions COMMON_OPTIONS = builder.build();
        return COMMON_OPTIONS;
    }

}
