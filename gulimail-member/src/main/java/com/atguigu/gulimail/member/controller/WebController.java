package com.atguigu.gulimail.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/memberOrder.html")
    public String memberOrder(){
        return "index";
    }

}
