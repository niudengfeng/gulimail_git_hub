package com.atguigu.gulimail.ware.service;

import com.atguigu.gulimail.ware.vo.PurchaseDoneVo;
import com.atguigu.gulimail.ware.vo.PurchaseVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 15:02:33
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils unreceiveList(Map<String, Object> params);

    void merge(PurchaseVo purchaseVo);

    void received(List<Long> items);

    void done(PurchaseDoneVo purchaseDoneVo);
}

