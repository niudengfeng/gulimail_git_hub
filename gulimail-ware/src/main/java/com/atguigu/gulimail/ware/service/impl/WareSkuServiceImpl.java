package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.common.utils.R;
import com.atguigu.common.vo.OrderItem;
import com.atguigu.common.vo.OrderLockVO;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimail.ware.service.feign.ProductFeignService;
import com.atguigu.gulimail.ware.vo.SkuHasStockWareVo;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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

    /**
     * 提交订单后锁定库存
     * @param orderLockVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public void lockStock(OrderLockVO orderLockVO) {
        //1.先根据订单项遍历得到 每个商品SKUID对应的有库存的仓库
        List<OrderItem> orderItems = orderLockVO.getOrderItems();
        List<SkuHasStockWareVo> skuHasStockWares = orderItems.stream().map(m -> {
            SkuHasStockWareVo skuHasStockWareVo = new SkuHasStockWareVo();
            List<Long> wareIds = dao.listWareIdsBySkuId(m.getSkuId());
            skuHasStockWareVo.setWareIds(wareIds);
            skuHasStockWareVo.setCount(m.getCount());
            skuHasStockWareVo.setSkuId(m.getSkuId());
            return skuHasStockWareVo;
        }).collect(Collectors.toList());

        //2.准备锁定库存
        for (SkuHasStockWareVo data : skuHasStockWares) {
            boolean lock = false;
            List<Long> wareIds = data.getWareIds();
            Long skuId = data.getSkuId();
            int count = data.getCount();
            if (CollectionUtils.isEmpty(wareIds)){
                throw new NoStockException(skuId);
            }
            //遍历这个商品对应的所有仓库，只要有一个仓库锁定成功即可
            for (Long wareId : wareIds) {
                int lockCount = dao.lockStock(skuId,wareId,count);
                if (lockCount>0){
                    //锁定成功
                    lock = true;
                    break;
                }
                //继续去下一个仓库锁定
            }
            if (!lock){
                //当前商品在所有仓库全部锁定失败
                throw new NoStockException(skuId);
            }
        }
        //能走到这里说明所有订单项对应库存全部锁定成功
    }

}
