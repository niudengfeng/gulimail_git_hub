package com.atguigu.gulimail.member.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *  TODO 问题1:我们feign远程调用会给我们重新封装个request请求,我们会丢失原请求的header,自然也就丢失了session cookie数据了
 *      *              * TODO 解决:封装的时候会循环判断所有的RequestInterceptor,所以我们需要配置个RequestInterceptor把当前请求的头信息带过去,人家遍历到这个RequestInterceptor,封装新请求就会带上了
 *      *              * TODO 问题2:开启异步变成有个问题就是会开多个线程去执行feign的调用,这样threadLocal的数据就不能共享了,所以又会丢请求头信息了,出现上面的问题
 *      *              * TODO 解决:每次远程调用之前都先执行:RequestContextHolder.setRequestAttributes(requestAttributes);把当前请求的reqeust信息取出来,然后大家调用之前先同步进去
 */

@Configuration
public class FeignConfig {

    @Bean(value = "requestInteceptor")
    public RequestInterceptor requestInteceptor(){
        RequestInterceptor requestInterceptor = requestTemplate -> {
            //1.先获取当前请求的头信息 ServletRequestAttributes 其实底层也是通过ThreadLocal来实现的
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            if (request!=null){
                String cookie = request.getHeader("Cookie");
                //2.同步到requestTemplate里面去
                requestTemplate.header("Cookie",cookie);
            }
        };
        return requestInterceptor;
    }

}
