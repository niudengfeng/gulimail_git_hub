package com.atguigu.gulimail.ware.service.impl;

import com.atguigu.gulimail.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimail.ware.service.PurchaseDetailService;
import com.atguigu.gulimail.ware.service.WareSkuService;
import com.atguigu.gulimail.ware.statusEnum.PurchaseDetailStatusEnum;
import com.atguigu.gulimail.ware.statusEnum.PurchaseStatusEnum;
import com.atguigu.gulimail.ware.vo.PurchaseDetailDoneVo;
import com.atguigu.gulimail.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimail.ware.vo.PurchaseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.ware.dao.PurchaseDao;
import com.atguigu.gulimail.ware.entity.PurchaseEntity;
import com.atguigu.gulimail.ware.service.PurchaseService;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;
    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> wrapper = new QueryWrapper<>();
        String status = String.valueOf(params.get("status")==null?"":params.get("status"));
        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",Integer.valueOf(status));
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().eq("assignee_id",key).or().like("assignee_name",key);
            });
        }
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils unreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status",0).or().eq("status",1)
        );
        return new PageUtils(page);
    }

    @Override
    public void merge(PurchaseVo purchaseVo) {
        List<Long> items = purchaseVo.getItems();
        if (items==null || items.size()==0){
            return;
        }
        Long purchaseId = purchaseVo.getPurchaseId();
        if(purchaseId==null){//没有采购单id 新增个采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(0);
            purchaseEntity.setPriority(0);
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        //判断当前采购单状态是否为0和1(新建或者已分配状态)
        PurchaseEntity byId = this.getById(purchaseId);
        if (byId.getStatus()==0 || byId.getStatus()==1){
            Long finalPurchaseId = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(i -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(1);
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        }
    }

    @Override
    public void received(List<Long> items) {
        if (items == null || items.size()==0){
            return;
        }
        //1.确认这些采购单是新建和已分配状态的（0和1）
        List<PurchaseEntity> purchaseEntities = items.stream().map(i -> this.getById(i)).filter(f -> {
            //过滤掉状态不在0和1的
            if (f.getStatus() == PurchaseStatusEnum.CREATE.getCode()
                    || f.getStatus() == PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            } else {
                return false;
            }
        }).map(p->{
            p.setStatus(PurchaseStatusEnum.RECEVED.getCode());
            p.setUpdateTime(new Date());
            return p;
        }).collect(Collectors.toList());
        //2.更改采购单状态为已领取
        this.updateBatchById(purchaseEntities);
        //3.更改采购项状态为正在采购 2
        if (purchaseEntities!=null && purchaseEntities.size()>0){
            for (PurchaseEntity purchaseEntity : purchaseEntities) {
                List<PurchaseDetailEntity> detailEntities = purchaseDetailService.listByPurchaseId(purchaseEntity.getId());
                if (detailEntities!=null && detailEntities.size()>0){
                    List<PurchaseDetailEntity> purchaseDetailEntities = detailEntities.stream().map(d -> {
                        PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                        detailEntity.setId(d.getId());
                        detailEntity.setStatus(PurchaseDetailStatusEnum.SHOPPING.getCode());
                        return detailEntity;
                    }).collect(Collectors.toList());
                    purchaseDetailService.updateBatchById(purchaseDetailEntities);
                }
            }
        }
    }

    @Override
    public void done(PurchaseDoneVo purchaseDoneVo) {
        Long id = purchaseDoneVo.getId();//采购单id
        List<PurchaseDetailDoneVo> items = purchaseDoneVo.getItems();
        //0.确认采购状态2已领取
        PurchaseEntity byId = this.getById(id);
        if (byId == null
                || byId.getStatus() != PurchaseStatusEnum.RECEVED.getCode()
                || items == null
                || items.size()==0
        ){
            return;
        }
        //2.更改采购项
        Boolean b = true;
        List<PurchaseDetailEntity> updateList = new ArrayList<>();
        for (PurchaseDetailDoneVo doneVo : items){
            Long detailId = doneVo.getItemId();
            if (doneVo.getStatus() == PurchaseDetailStatusEnum.HASERROR.getCode()){
                //采购失败
                b = false;
            }else {
                //采购成功
                //3.入库 wms_ware_sku
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(detailId);
                if (detailEntity!=null){
                    Long skuId = detailEntity.getSkuId();
                    Integer skuNum = detailEntity.getSkuNum();
                    Long wareId = detailEntity.getWareId();
                    wareSkuService.addStock(skuId,wareId,skuNum);
                }
            }
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(detailId);
            purchaseDetailEntity.setStatus(doneVo.getStatus());
            updateList.add(purchaseDetailEntity);
        }
        if (updateList.size()>0){
            purchaseDetailService.updateBatchById(updateList);
        }
        //1.更改采购单状态依据采购项状态 如果所有采购项状态都是已完成 那么已完成 否则有异常 <>
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(b?PurchaseStatusEnum.FINISH.getCode():PurchaseStatusEnum.HASERROR.getCode());
        this.baseMapper.updateById(purchaseEntity);
    }

}