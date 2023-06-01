package com.atguigu.gulimail.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.coupon.dao.SeckillSessionDao;
import com.atguigu.gulimail.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimail.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSeckillProductCurrentThreeDays() {
        return this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time",getStartTime(),getEndTime()));
    }

    private static String getStartTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime of = LocalDateTime.of(now, min);
        return of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String getEndTime(){
        LocalDate now = LocalDate.now();//当前日期2-18
        LocalDate end = now.plusDays(2);//当前日期加两天2-20
        LocalTime max = LocalTime.MAX;
        LocalDateTime of = LocalDateTime.of(end, max);
        return of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void main(String[] args) {
        System.out.println(getStartTime());
        System.out.println(getEndTime());

    }
}
