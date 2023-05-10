package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.gulimail.ware.entity.WareInfoEntity;
import com.atguigu.gulimail.ware.service.feign.ProductFeignService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.ware.dao.WareSkuDao;
import com.atguigu.gulimail.ware.entity.WareSkuEntity;
import com.atguigu.gulimail.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao dao;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<WareSkuEntity>();
        String skuId = String.valueOf(params.get("skuId")==null?"0":params.get("skuId"));
        if (!StringUtils.isEmpty(skuId) && Long.parseLong(skuId)!=0L){
            wrapper.eq("sku_id",Long.parseLong(skuId));
        }
        String wareId = String.valueOf(params.get("wareId")==null?"0":params.get("wareId"));
        if (!StringUtils.isEmpty(wareId) && Long.parseLong(wareId)!=0L){
            wrapper.eq("ware_id",Long.parseLong(wareId));
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1.先根据skuId，wareId查询 是否存在库存，没有就新增，否则更改
        List<WareSkuEntity> list = this.list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (list!=null && list.size()>0){
            //修改库存
            dao.updateByWrapper(skuId,wareId,skuNum);
        }else {
            //新增库存
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            try{
                R info = productFeignService.info(skuId);
                if ((Integer) info.get("code") == 0){
                    Map<String,Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error("远程调用product服务获取skuName异常",e);
            }
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            dao.insert(wareSkuEntity);
        }
    }

    @Override
    public Map<Long,Boolean> hasStock(List<Long> skuIds) {
        Map<Long,Boolean> stockMap = new HashMap();
        if (skuIds == null || skuIds.size() == 0){
            return stockMap;
        }
        skuIds.forEach(skuId->{
            Integer integer = dao.hasStockBySkuId(skuId);
            stockMap.put(skuId,integer>0);
        });
        return stockMap;
    }

}