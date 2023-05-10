package com.atguigu.gulimail.product.service.impl;

import com.atguigu.gulimail.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimail.product.dao.AttrDao;
import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimail.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.apache.commons.lang.StringUtils;
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

import com.atguigu.gulimail.product.dao.AttrGroupDao;
import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrService attrService;
    @Resource
    private AttrAttrgroupRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catId) {
        IPage<AttrGroupEntity> page = null;
        Object key = params.get("key");
        if (catId == 0){//默认查所有
            QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_group_id", key.toString()).or().like("attr_group_name", key.toString());
            page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
        } else {
            QueryWrapper<AttrGroupEntity> attrGroupEntityQueryWrapper = new QueryWrapper<AttrGroupEntity>().eq("catelog_id",catId);
            String paramKey = "";
            if (key!=null){
                paramKey = key.toString();
                if (StringUtils.isNotEmpty(paramKey)){
                    String finalParamKey = paramKey;
                    attrGroupEntityQueryWrapper.and((obj)->{
                        obj.eq("attr_group_id", finalParamKey).or().like("attr_group_name", finalParamKey);
                    });
                }
            }
            page = this.page(new Query<AttrGroupEntity>().getPage(params),attrGroupEntityQueryWrapper);
        }
        return new PageUtils(page);
    }

    /**
     * 查询所有未关联过的属性
     * @param attrGroupId
     * @return
     */
    @Override
    public PageUtils listByAttrGroupIdNo(Map<String, Object> params,Long attrGroupId) {
        //sql:SELECT * from pms_attr where attr_type = '1' and attr_id not in (SELECT attr_id from pms_attr_attrgroup_relation)
        //1.先查出所有已经关联过的属性id集合
        List<AttrAttrgroupRelationEntity> relations = relationService.getBaseMapper().selectList(new QueryWrapper<AttrAttrgroupRelationEntity>());
        List<Long> attrList = null;
        if(relations!=null && relations.size()>0){
           attrList = relations.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        }
        //2.查询所有属性集合根据条件 id不在已关联的属性id集合中
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("attr_type","1")
                .notIn("attr_id",attrList);
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)){
            wrapper.and(w->{
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public List<AttrEntity> listByAttrGroupId(Long attrGroupId) {
        //先根据attrgroupid查询出对应的attrid,再根据attrid查找对应的属性实体列表
        Wrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id",attrGroupId);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = relationService.getBaseMapper().selectList(queryWrapper);
        List<Long> attrList = null;
        List<AttrEntity> attrEntities = null;
        if (attrAttrgroupRelationEntities!=null){
            attrList = attrAttrgroupRelationEntities.stream().map(item -> item.getAttrId() ).collect(Collectors.toList());
        }
        if (null != attrList && attrList.size()>0){
            attrEntities = attrService.getBaseMapper().selectBatchIds(attrList);
        }
        return attrEntities;
    }

    @Override
    public List<AttrGroupEntity> withattr(Long catelogId) {
        //1.根据分类id得到对应分组
        Map columnmap = new HashMap();
        columnmap.put("catelog_id",catelogId);
        List<AttrGroupEntity> list = this.listByMap(columnmap);
        if (list!=null && list.size()>0){
            //2.根据分组id去关联表找到所有对应属性id集合
            list.forEach(attrGroupEntity -> {
                Map map = new HashMap();
                map.put("attr_group_id",attrGroupEntity.getAttrGroupId());
                List<AttrAttrgroupRelationEntity> listByMap = relationService.listByMap(map);
                if (listByMap!=null && listByMap.size()>0){
                    //3.得到所有属性id集合
                    List<Long> attrIds = listByMap.stream().map(attrs -> attrs.getAttrId()).collect(Collectors.toList());
                    if (attrIds!=null && attrIds.size()>0){
                        //4.根据所有属性Id集合找到对应属性实体
                        List<AttrEntity> attrEntities = attrService.listByIds(attrIds);
                        attrGroupEntity.setAttrs(attrEntities);
                    }
                }
            });
        }
        return list;
    }

}