package com.atguigu.gulimail.member.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.member.feign.FeignOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private FeignOrder feignOrder;

    /**
     * 分页查询我的所有订单以及每个订单对应的订单明细项
     * @return
     */
    @GetMapping("/memberOrder.html")
    public String memberOrder(@RequestParam(value = "page",defaultValue = "1") String page,@RequestParam(value = "limit",defaultValue = "20") String limit, Model model){
        Map params = new HashMap();
        params.put(Constant.PAGE,page);
        params.put(Constant.LIMIT,limit);
        R orderPage = feignOrder.getOrderPage(params);
        if (orderPage.getCode() == 0){
            PageUtils p = JSON.parseObject(JSON.toJSONString(orderPage.get("page")), PageUtils.class);
            model.addAttribute("page",p);
        }
        return "index";
    }

}
