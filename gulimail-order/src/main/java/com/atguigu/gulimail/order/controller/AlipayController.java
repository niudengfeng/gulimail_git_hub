package com.atguigu.gulimail.order.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gulimail.order.entity.OrderEntity;
import com.atguigu.gulimail.order.entity.OrderItemEntity;
import com.atguigu.gulimail.order.service.OrderItemService;
import com.atguigu.gulimail.order.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemService itemService;
    @Autowired
    private AlipayClient alipayClient;

    @Value("${ali.pay.notifyUrl}")
    private String notifyUrl;
    @Value("${ali.pay.returnUrl}")
    private String returnUrl;

    @ResponseBody
    @GetMapping(value = "/go" , produces = {"text/html"})
    public String alipay(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        OrderEntity orderStatus = orderService.getOrderStatus(orderSn);
        BigDecimal payAmount = orderStatus.getPayAmount();
        QueryWrapper<OrderItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_sn",orderSn);
        String skuName = itemService.getBaseMapper().selectList(queryWrapper).get(0).getSkuName();
        //异步接收地址，仅支持http/https，公网可访问
        request.setNotifyUrl(notifyUrl);
        //同步跳转地址，仅支持http/https
        request.setReturnUrl(returnUrl);
        /******必传参数******/
        JSONObject bizContent = new JSONObject();
        //商户订单号，商家自定义，保持唯一性
        bizContent.put("out_trade_no", orderSn);
        //支付金额，最小值0.01元
        bizContent.put("total_amount", payAmount);
        //订单标题，不可使用特殊符号
        bizContent.put("subject", skuName);
        //电脑网站支付场景固定传值FAST_INSTANT_TRADE_PAY
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        request.setBizContent(bizContent.toString());
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
            String jsonString = JSON.toJSONString(response);
            log.info("返回消息:"+jsonString);
            String body = response.getBody();
            return body;
        } else {
            System.out.println("调用失败");
            return "error";
        }
    }

    @GetMapping("/success")
    public String success(){

        return "pay";
    }

    @GetMapping("/notify")
    public String notifySuccess(){
        return "pay";
    }

}
