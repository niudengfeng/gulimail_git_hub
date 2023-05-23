package com.gulimail.third.controller;

import com.atguigu.common.utils.R;
import com.gulimail.third.compoment.SmsSendServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
@Api(tags = "短信发送服务")
public class SmsController {

    @Autowired
    private SmsSendServer smsSendServer;

    @ApiOperation("发送短信验证码")
    @RequestMapping(value = "/sendCode",method = RequestMethod.GET)
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsSendServer.sendCode(phone,code);
        return R.ok();
    }
}
