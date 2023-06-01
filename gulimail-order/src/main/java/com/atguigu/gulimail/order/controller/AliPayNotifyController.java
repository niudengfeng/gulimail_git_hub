package com.atguigu.gulimail.order.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gulimail.order.config.AlipayClientConfig;
import com.atguigu.gulimail.order.service.OrderService;
import com.atguigu.gulimail.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RestController
public class AliPayNotifyController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayClient alipayClient;

    @Autowired
    private AlipayClientConfig alipayClientConfig;

    @RequestMapping("/alipay/notify")
    public String notifyAlipay(PayAsyncVo vo, HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("收到支付宝支付成功回调通知："+ JSON.toJSONString(parameterMap));
        try {
            if (signVerified(parameterMap)){
                log.info("验签成功，是支付宝发来的请求");
                orderService.dealAlipayNotify(vo);
                return "success";
            }else {
                log.error("非支付宝回调");
                return "error";
            }
        } catch (Exception e) {
            log.error("处理支付宝通知异常:",e);
            return "error";
        }
    }

    private boolean signVerified(Map<String,String[]> requestParams) throws AlipayApiException, UnsupportedEncodingException {
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayClientConfig.getPublicKey(),
                AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2); //调用SDK验证签名

        return signVerified;
    }

}
