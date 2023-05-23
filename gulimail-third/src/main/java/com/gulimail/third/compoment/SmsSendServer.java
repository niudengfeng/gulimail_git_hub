package com.gulimail.third.compoment;

import com.atguigu.common.utils.HttpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@Component
@ConfigurationProperties("spring.cloud.alicloud")
@RefreshScope
public class SmsSendServer {
    @Value("${spring.cloud.alicloud.sms.host}")
    private String host;
    @Value("${spring.cloud.alicloud.sms.path}")
    private String path;
    @Value("${spring.cloud.alicloud.sms.method}")
    private String method;
    @Value("${spring.cloud.alicloud.sms.appcode}")
    private String appcode;
    @Value("${spring.cloud.alicloud.sms.content}")
    private String content;
    @Value("${spring.cloud.alicloud.sms.minute}")
    private String minute;
    @Value("${spring.cloud.alicloud.sms.smsSignId}")
    private String smsSignId;
    @Value("${spring.cloud.alicloud.sms.templateId}")
    private String templateId;

    public void sendCode(String phone, String code){
        Map<String, String> headers = new HashMap();
        headers.put("Authorization", "APPCODE " + appcode);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        Map<String, String> bodys = new HashMap();
        bodys.put("phone_number", phone);
        content = "code:str";
        content = StringUtils.replace(content, "str", code.toString());
        content = StringUtils.replace(content, "minute", minute);
        bodys.put("content", content);
//        querys.put("smsSignId", smsSignId);
        bodys.put("template_id", templateId);
        Map<String, String> querys = new HashMap();
        try {
            log.info("开始发送短信手机号："+phone+",内容如下："+content);
            HttpResponse response = HttpUtil.doPost(host, path, method, headers, querys, bodys);
            String string = EntityUtils.toString(response.getEntity());
            log.info("调用短信接口响应："+string);
        } catch (Exception e) {
            log.error("发送短信验证码失败，异常："+e.getMessage());
        }
    }

}




