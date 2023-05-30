package com.atguigu.gulimail.order.feign;

import com.atguigu.common.utils.R;
import com.atguigu.common.vo.MemberAddressVo;
import com.atguigu.common.vo.OrderLockVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "gulimail-ware")
public interface WareFeign {
    @RequestMapping("/ware/waresku/hasStock")
    public R hasStock(@RequestBody List<Long> skuIds);

    @RequestMapping("/ware/waresku/lockStock")
    public R lockStock(@RequestBody OrderLockVO orderLockVO);
}
