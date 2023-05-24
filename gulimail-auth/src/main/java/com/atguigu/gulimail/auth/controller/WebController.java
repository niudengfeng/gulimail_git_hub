package com.atguigu.gulimail.auth.controller;

import com.atguigu.common.utils.RedisConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class WebController {

   @GetMapping("/login.html")
    public String login(HttpSession session){
       if (session.getAttribute(RedisConstants.SESSION_USER_KEY)!=null){
           return "redirect:http://gulimail.com";//登录过了，直接去首页
       }
       return "index";//去登录页
    }

    /**
    @GetMapping("/reg.html")
    public String reg(){
        return "reg";
    }*/

}
