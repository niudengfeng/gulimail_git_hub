package com.atguigu.gulimail.search.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.search.entity.User;
import com.atguigu.gulimail.search.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "用户ES增删改查测试")
@RequestMapping("/api/es/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "保存")
    @PostMapping("/save")
    public R save(@RequestBody User user){
        User save = userService.save(user);
        return R.ok().put("user",save);
    }
}
