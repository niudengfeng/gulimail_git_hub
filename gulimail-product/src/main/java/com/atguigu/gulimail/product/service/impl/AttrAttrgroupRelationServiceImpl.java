package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.vo.AttrAttrgroupRelationVo;
import com.fasterxml.jackson.databind.BeanProperty;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void addByAttrGroupRelations(AttrAttrgroupRelationVo[] relationEntities) {
        List<AttrAttrgroupRelationEntity> entityList = Arrays.stream(relationEntities).map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            relationEntity.setAttrSort(0);
            return relationEntity;
        }).collect(Collectors.toList());
        this.saveBatch(entityList);
    }

    @Override
    public void delByAttrGroupRelations(AttrAttrgroupRelationVo[] relationEntities) {
        if (relationEntities!=null){
            Arrays.stream(relationEntities).forEach((item)->{
                long attrgroupid = item.getAttrGroupId().longValue();
                long attrid = item.getAttrId().longValue();
                QueryWrapper<AttrAttrgroupRelationEntity> eq = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrid).eq("attr_group_id", attrgroupid);
                this.baseMapper.delete(eq);
            });
        }
    }

}