package com.atguigu.common.config;

import cn.hutool.http.ContentType;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.BizCode;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SentinelConfig {

    /**
     * 降级后的返回结果处理
     * 这里直接写个错误返回
     * 建议处理：
     *  1.直接返回错误友好提示
     *  2.返回自定义的错误页面
     *  3.发送mq记录错误
     */
    @PostConstruct
    public void init(){
        WebCallbackManager.setUrlBlockHandler((httpServletRequest, httpServletResponse, e) -> {
            R error = R.error(BizCode.REQUEST_TOO_FAST.getCode(), BizCode.REQUEST_TOO_FAST.getMsg());
            httpServletResponse.setContentType(ContentType.JSON.getValue());
            httpServletResponse.setCharacterEncoding("utf-8");
            httpServletResponse.getWriter().write(JSON.toJSONString(error));
        });
    }

}
