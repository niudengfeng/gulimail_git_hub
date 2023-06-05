package com.atguigu.gulimail.seckill.controller.web;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.atguigu.gulimail.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sentinel")
public class SentinelResourceController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/resource")
    public String resource(@RequestParam("code") String code){
        if (code.equals("1")){
            return seckillService.normalResource(code);
        }else {
            try (Entry entry = SphU.entry("sentinelResourceName")){
                Thread.sleep(100);
            } catch (BlockException e) {
             log.error("自定义资源限流:{}",e.getMessage());
             return "error";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("正常");
            return "ok==>"+code;
        }
    }

    @GetMapping("/hello")
    @SentinelResource(value = "hello",blockHandler = "helloHandler")
    public String hello(@RequestParam("code") String code){
        return "hello"+code;
    }

    public String helloHandler(String code,BlockException e){
        return "hello error";
    }
}
