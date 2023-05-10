package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.gulimail.ware.entity.WareInfoEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.ware.dao.PurchaseDetailDao;
import com.atguigu.gulimail.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimail.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * key: 111
         * status: 1
         * wareId: 1
         */
        QueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().eq("purchase_id",key).or().eq("sku_id",key);
            });
        }
        String status = String.valueOf(params.get("status")==null?"":params.get("status"));
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",Integer.valueOf(status));
        }
        String wareId = String.valueOf(params.get("wareId")==null?"0":params.get("wareId"));
        if (!StringUtils.isEmpty(wareId) && Long.parseLong(wareId)!=0L){
            wrapper.eq("ware_id",Long.parseLong(wareId));
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listByPurchaseId(Long id) {
        return this.baseMapper.selectList(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id",id));
    }

}