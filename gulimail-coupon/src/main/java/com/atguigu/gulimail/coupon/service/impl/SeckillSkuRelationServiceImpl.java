package com.atguigu.gulimail.coupon.service.impl;

import com.atguigu.common.vo.SeckillSessionRedisTo;
import com.atguigu.common.vo.SeckillSkuRelationRedisTo;
import com.atguigu.gulimail.coupon.entity.SeckillSessionEntity;
import com.atguigu.gulimail.coupon.service.SeckillSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimail.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimail.coupon.service.SeckillSkuRelationService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Autowired
    private SeckillSessionService seckillSessionService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> q = new QueryWrapper<>();
        //场次ID不为空
        Object promotionSessionId = params.get("promotionSessionId");
        if (!StringUtils.isEmpty(promotionSessionId)){
            q.eq("promotion_session_id",promotionSessionId);
        }
        q.orderByDesc("id");
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                q
        );
        return new PageUtils(page);
    }


    @Override
    public List<SeckillSessionRedisTo> getSeckillProductCurrentThreeDays() {
        List<SeckillSessionEntity> seckillSessionEntities = seckillSessionService.getBaseMapper().selectList(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));
        if (! CollectionUtils.isEmpty(seckillSessionEntities)){
            return seckillSessionEntities.stream().map(m->{
                SeckillSessionRedisTo to = new SeckillSessionRedisTo();
                BeanUtils.copyProperties(m,to);
                QueryWrapper<SeckillSkuRelationEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("promotion_session_id",m.getId());
                List<SeckillSkuRelationEntity> seckillSkuRelationEntities = this.baseMapper.selectList(wrapper);
                if (!CollectionUtils.isEmpty(seckillSkuRelationEntities)){
                    List<SeckillSkuRelationRedisTo> collect = seckillSkuRelationEntities.stream().map(d -> {
                        SeckillSkuRelationRedisTo seckillSkuRelationRedisTo = new SeckillSkuRelationRedisTo();
                        BeanUtils.copyProperties(d, seckillSkuRelationRedisTo);
                        return seckillSkuRelationRedisTo;
                    }).collect(Collectors.toList());
                    to.setRelationRedisTos(collect);
                }
                return to;
            }).collect(Collectors.toList());
        }
        return null;
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
