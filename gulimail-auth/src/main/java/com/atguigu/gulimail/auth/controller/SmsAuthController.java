package com.atguigu.gulimail.auth.controller;

import com.atguigu.common.utils.BusinessCode;
import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.utils.RedomUtil;
import com.atguigu.gulimail.auth.feign.ThirdFeignService;
import com.atguigu.gulimail.auth.vo.RegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/sms")
@RefreshScope
public class SmsAuthController {

    @Autowired
    private ThirdFeignService thirdFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${sms.code.ttl}")
    private Integer codeTtl;//短信验证码的重发时间（秒）

    @Value("${sms.code.expire}")
    private Integer expire;//短信验证码的过期时间（分钟）

    @RequestMapping(value = "/sendCode",method = RequestMethod.GET)
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        //定义key
        String key = RedisConstants.SMS_CODE_PREFIX+phone;
        //1.先判断当前redis中是否存在当前手机号对应验证码，然后判断时间是否超过60s
        String value = redisTemplate.opsForValue().get(key);
        if (! StringUtils.isEmpty(value) ){
            String s = value.split("_")[1];
            long inTime = Long.valueOf(s);
            if(System.currentTimeMillis()/1000-inTime<60){
                return R.error(BusinessCode.SMSSENDFAST.getCode(),BusinessCode.SMSSENDFAST.getMessage());
            }
        }
        //2.生产短信CODE
        Integer code = RedomUtil.randomCount(111111, 999999);
        String codeValue = code + "_" + System.currentTimeMillis()/1000;//秒级别
        //3.保存入redis
        redisTemplate.opsForValue().set(key,codeValue,Long.valueOf(expire), TimeUnit.MINUTES);
        return R.ok(thirdFeignService.sendCode(phone,code.toString()));
    }
}
