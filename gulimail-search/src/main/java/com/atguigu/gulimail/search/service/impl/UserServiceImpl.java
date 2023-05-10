package com.atguigu.gulimail.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimail.search.entity.User;
import com.atguigu.gulimail.search.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public User save(User user) {
        try {
            IndexRequest indexRequest = new IndexRequest("user");
            indexRequest.id();
            IndexRequest source = indexRequest.source(JSONObject.toJSONString(user), XContentType.JSON);
            IndexResponse index = client.index(source, RequestOptions.DEFAULT);
            log.info("保存："+index.toString());
        } catch (IOException e) {
            log.error("保存用户数据到ES异常",e.getMessage());
        }finally {
            return user;
        }
    }

    @Override
    public boolean del(Integer id) {
        return false;
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public List<User> selectList(Map map) {
        return null;
    }
}
