package com.ndf.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    void contextLoads() {
        System.out.println(client);
    }

}
