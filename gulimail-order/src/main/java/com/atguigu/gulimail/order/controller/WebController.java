package com.atguigu.gulimail.order.controller;

import com.atguigu.gulimail.order.service.OrderService;
import com.atguigu.gulimail.order.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{page}.html")
    public String toPage(@PathVariable("page") String page){
        return page;
    }

    /**
     * 用户在购物车界面点击去结算进入这个接口
     * 这里需要查询信息后返回到订单确认页
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model){
        OrderVo order = orderService.toTrade();
        model.addAttribute("order",order);
//        System.out.println(order);
        return "confirm";
    }

}
