package com.atguigu.gulimail.auth.controller;

import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.gulimail.auth.feign.MemberFeignService;
import com.atguigu.gulimail.auth.vo.RegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;

@Controller
public class RegistAuthController {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     *  TODO 重定向携带数据是利用了session原理，将数据存放再session中 。重定向到对应页面，从session中取出数据后，会进行清除，
     *  TODO 分布式session需要解决
     * @param registVo
     * @param result
     * @param redirectAttributes 重定向视图，携带数据
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid RegistVo registVo, BindingResult result,
                         RedirectAttributes redirectAttributes){
        if (result.hasErrors()){
            HashMap<String, String> errors = new HashMap<>();
            //封装错误消息给前端
            //       java8     list转map
//            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            result.getFieldErrors().stream().forEach(fieldError -> {
                String field = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                errors.put(field,defaultMessage);
            });
//            model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors",errors);
            /**
             *  return "forward:/reg.html";//Request method 'POST' not supported
             *  因为forward转发到reg.html 走的是
             *      public void addViewControllers(ViewControllerRegistry registry) {
             *         registry.addViewController("/login.html").setViewName("index");
             *         registry.addViewController("/reg.html").setViewName("reg");
             *     }
             *     上面这个默认GET请求，所以报错
             *     改成下面方式
             */
//           return "reg";//会有刷新页面重复提交的问题，改为下面的重定向

            return "redirect:http://auth.gulimail.com/reg.html";//重定向页面获取不到model里面的 数据需要引入 RedirectAttributes
        }
        //远程调用会员服务进行注册
        //1.校验验证码，如果成功 调用member服务
        //2.否则返回注册页，带上错误信息
        //注册成功回到登录页
        //定义key
        String key = RedisConstants.SMS_CODE_PREFIX+registVo.getPhone();
        String value = redisTemplate.opsForValue().get(key);
        if (! StringUtils.isEmpty(value) //从redis中获取到值并且不为空
                && (value.split("_")[0].equals(registVo.getCode()))//取出来的code和页面传过来的比较是一样的
        ){
            //删除redis中的对应key，防止重复校验
            redisTemplate.delete(key);
            //远程调用会员服务进行注册
            R regist = memberFeignService.regist(registVo);
            if (regist.getCode()!=0){
                HashMap<Object, Object> err = new HashMap<>();
                err.put("msg",regist.get("msg"));
                redirectAttributes.addFlashAttribute("errors",err);
                return "redirect:http://auth.gulimail.com/reg.html";//重定向页面获取不到model里面的 数据需要引入 RedirectAttributes
            }
            return "redirect:http://auth.gulimail.com/login.html";
        }
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("code","验证码校验错误");
        redirectAttributes.addFlashAttribute("errors",objectObjectHashMap);
        return "redirect:http://auth.gulimail.com/reg.html";//重定向页面获取不到model里面的 数据需要引入 RedirectAttributes
    }
}
