package com.atguigu.gulimail.order.controller;

import com.atguigu.gulimail.order.vo.SubmitOrderResponseVo;
import com.atguigu.common.vo.SubmitOrderVo;
import com.atguigu.gulimail.order.service.OrderService;
import com.atguigu.gulimail.order.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
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
        return "confirm";
    }

    /**
     * 表单提交 创建订单
     * @param submitOrderVo
     * @return
     */
    @PostMapping("/createOrder")
    public String createOrder(SubmitOrderVo submitOrderVo, RedirectAttributes redirectAttributes){
        //去创建订单，验令牌，验价格单
        // 成功来到支付选择页
        // 下单失败回到订单确认页  重新确认订单信息
        log.info("提交的订单信息："+submitOrderVo);
        SubmitOrderResponseVo responseVo = orderService.createOrder(submitOrderVo);
        redirectAttributes.addAttribute("responseVo",responseVo);
        if (responseVo.getCode()==0){
            //成功跳到pay支付页面
            return "pay";
        }else {
            //失败了重新回到支付确认页面。
            return "redirect:http://order.gulimail.com/toTrade";
        }
    }
}
