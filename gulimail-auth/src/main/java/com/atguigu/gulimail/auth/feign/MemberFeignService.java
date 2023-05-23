package com.atguigu.gulimail.auth.feign;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.auth.vo.LoginVo;
import com.atguigu.gulimail.auth.vo.RegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("gulimail-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    public R regist(@RequestBody RegistVo vo);
    @PostMapping("/member/member/login")
    public R login(@RequestBody LoginVo vo);
}
