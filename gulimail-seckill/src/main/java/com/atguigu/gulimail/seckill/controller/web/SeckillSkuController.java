package com.atguigu.gulimail.seckill.controller.web;

import com.atguigu.common.utils.R;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.common.vo.MemberVO;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.gulimail.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RequestMapping("/seckill/kill")
@Controller
public class SeckillSkuController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/order")
    public String kill(@RequestParam("skuId_promotionSessionId") String skuId_promotionSessionId,
                  @RequestParam("token") String token,
                  @RequestParam("count") String count,Model model){
        String orderSn = seckillService.kill(skuId_promotionSessionId,token,count);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }

}
