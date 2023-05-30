package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.common.constants.MqConstants;
import com.atguigu.common.dto.mq.StockLockedTo;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.OrderItem;
import com.atguigu.common.vo.OrderLockVO;
import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimail.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimail.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimail.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimail.ware.service.WareOrderTaskService;
import com.atguigu.gulimail.ware.service.feign.ProductFeignService;
import com.atguigu.gulimail.ware.vo.SkuHasStockWareVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    private WareOrderTaskService taskService;

    @Autowired
    private WareOrderTaskDetailService taskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
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
     * 以下两种情况需要根据工作单解锁对应仓库的商品数量
     * 1.正常锁定，然后用户取消订单了,
     * 2.锁定成功，order服务报错，事务回滚
     * 以上两种情况都是锁定库存方法执行成功，如果报错，所库存失败，无需解锁，因为下面是在一个本地事务，（部分成功锁定的情况）但是消息发出去了，处理消息的时候根据taskId查询，查不到不处理，因为回滚了task
     * @param orderLockVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public void lockStock(OrderLockVO orderLockVO) {
        /**
         * 保存库存工作单，能回滚 追溯
         */
        WareOrderTaskEntity task = new WareOrderTaskEntity();
        task.setOrderSn(orderLockVO.getOrderSn());
        taskService.save(task);
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
                    WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity();
                    taskDetail.setSkuId(skuId);
                    taskDetail.setWareId(wareId);
                    taskDetail.setSkuNum(count);
                    taskDetail.setLockStatus(1);
                    taskDetail.setTaskId(task.getId());
                    taskDetailService.save(taskDetail);

                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setTaskId(task.getId());
                    stockLockedTo.setTaskDetailId(taskDetail.getId());
                    //TODO 锁定成功，发送消息通知MQ,让他去检查这个sku对应订单是否正常，不正常需要回滚
                    rabbitTemplate.convertAndSend(MqConstants.stockEventExchange,MqConstants.stockLockRoutingKey,stockLockedTo);
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
