package com.atguigu.gulimail.product.service;

import com.atguigu.gulimail.product.vo.AttrAttrgroupRelationVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;

import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 14:57:19
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addByAttrGroupRelations(AttrAttrgroupRelationVo[] relationEntities);

    void delByAttrGroupRelations(AttrAttrgroupRelationVo[] relationEntities);
}

