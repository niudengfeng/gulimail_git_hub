package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimail.product.service.AttrGroupService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.AttrDao;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrDao attrDao;
    @Autowired
    private AttrGroupService groupService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (key!=null){
            String keys = key.toString();
            if (StringUtils.isNotEmpty(keys)){
                wrapper.like("attr_name",keys).or().like("catelog_name",keys);
            }
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByType(Map<String, Object> params) {
        String key = (String) params.get("key");
        String type = (String) params.get("type");
        Long catId = (Long) params.get("catId");
        Long attrType = "base".equalsIgnoreCase(type)?1L:0L;
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("attr_type",attrType);
        if (catId!=0){
            wrapper.and((i)->{
               i.eq("catelog_id",catId);
            });
        }
        if (StringUtils.isNotEmpty(key)){
            wrapper.and((w1)->{
                w1.like("attr_name",key).or().eq("attr_id",key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        page.getRecords().forEach(item->{
            Long attrId = item.getAttrId();
            Map columnMap = new HashMap();
            columnMap.put("attr_id",attrId);
            List<AttrAttrgroupRelationEntity> list = relationService.listByMap(columnMap);
            Long attrgroupid=null;
            if (list!=null && list.size()>0){
                attrgroupid = list.get(0).getAttrGroupId();
            }
            String groupName = "";
            if (attrgroupid!=null){
                AttrGroupEntity byId = groupService.getById(attrgroupid);
                if (byId!=null){
                    groupName = byId.getAttrGroupName();
                }
            }
            item.setGroupName(groupName);
        });
        return new PageUtils(page);
    }

    @Override
    public void saveRelation(AttrEntity attr) {
        if (attr.getAttrGroupId()!=null && attr.getAttrType() == 1){//只有属性分组不为空且基本属性才可以关联属性分组
            //如果选择了属性分组，需要给他保存对应映射表中去
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrSort(0);
            relationEntity.setAttrId(attr.getAttrId());
            UpdateWrapper<AttrAttrgroupRelationEntity> updateWrapper = new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId());
            relationService.saveOrUpdate(relationEntity,updateWrapper);
        }
    }

    @Override
    public List<Long> getSearchDataByAttrIds(List<Long> attrIds) {
        return attrDao.getSearchDataByAttrIds(attrIds);
    }

}