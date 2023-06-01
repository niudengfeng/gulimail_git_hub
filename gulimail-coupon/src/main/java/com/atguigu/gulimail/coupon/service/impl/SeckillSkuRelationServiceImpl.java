package com.atguigu.gulimail.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.coupon.dao.SeckillSkuRelationDao;
import com.atguigu.gulimail.coupon.entity.SeckillSkuRelationEntity;
import com.atguigu.gulimail.coupon.service.SeckillSkuRelationService;
import org.springframework.util.StringUtils;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

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

}
