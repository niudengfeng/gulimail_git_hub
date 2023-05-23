package com.atguigu.gulimail.auth.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.gulimail.auth.feign.MemberFeignService;
import com.atguigu.gulimail.auth.vo.LoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
public class LoginAuthController {
    @Autowired
    private MemberFeignService memberFeignService;

    @PostMapping("/login")
    public String login(@Valid LoginVo loginVo, BindingResult result,
                        RedirectAttributes redirectAttributes, HttpSession session){
        R r = memberFeignService.login(loginVo);
        if (r.getCode().intValue()!=0){
            HashMap<String, String> errors = new HashMap<>();
            errors.put("msg", (String) r.get("msg"));
            redirectAttributes.addFlashAttribute("errors",errors);
            //登录失败
            return "redirect:http://auth.gulimail.com/login.html";
        }
        LinkedHashMap o = (LinkedHashMap) r.get("user");
        String jsonString = JSON.toJSONString(o);
        MemberVO memberVO1 = JSON.parseObject(jsonString, MemberVO.class);
        //登录成功，把用户信息放入缓存，回显首页相关用户信息
        session.setAttribute("user",memberVO1);
        //如果成功返回首页
        return "redirect:http://gulimail.com";//重定向页面获取不到model里面的 数据需要引入 RedirectAttributes
    }
}
