package com.atguigu.gulimail.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.seckill.entity.SeckillSessionEntity;
import com.atguigu.gulimail.seckill.service.UpSeckillProductService;
import com.atguigu.gulimail.seckill.service.feign.CouponFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UpSeckillProductServiceImpl implements UpSeckillProductService {

    @Autowired
    private CouponFeign couponFeign;


    @Override
    public void upSeckillProduct() {

        //1.扫描需要上架的秒杀商品，远程调用
        R r = couponFeign.getSeckillProductCurrentThreeDays();
        if (r.getCode() == 0){
            List<SeckillSessionEntity> data = (List<SeckillSessionEntity>) JSON.parseObject(JSON.toJSONString(r.get("data")), ArrayList.class);
        }
    }
}
