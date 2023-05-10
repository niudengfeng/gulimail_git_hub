package com.atguigu.gulimail.product.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product/express")
public class ExpressTestController {

    @GetMapping("/test1")
    public String test1(){
        return "hello,test1";
    }

}
