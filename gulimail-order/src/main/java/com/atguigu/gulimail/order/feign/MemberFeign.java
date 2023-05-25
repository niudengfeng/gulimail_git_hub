package com.atguigu.gulimail.order.feign;

import com.atguigu.common.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "gulimail-member")
public interface MemberFeign {
    @GetMapping("/member/memberreceiveaddress/getListByMemberId/{memberId}")
    List<MemberAddressVo> getListByMemberId(@PathVariable("memberId") Long memberId);
}
