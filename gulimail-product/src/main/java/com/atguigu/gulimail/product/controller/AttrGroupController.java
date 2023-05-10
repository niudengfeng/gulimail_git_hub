package com.atguigu.gulimail.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimail.product.vo.AttrAttrgroupRelationVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimail.product.entity.AttrGroupEntity;
import com.atguigu.gulimail.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 属性分组
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 16:20:19
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 根据属性分组id查询所有未关联的属性列表
     */
    @RequestMapping("/{attrGroupId}/noattr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R listByAttrGroupIdNo(@RequestParam Map<String, Object> params,@PathVariable("attrGroupId") Long attrGroupId){
        PageUtils page = attrGroupService.listByAttrGroupIdNo(params,attrGroupId);
        return R.ok().put("page", page);
    }

    /**
     * 根据属性分组id查询所有关联的属性列表
     */
    @RequestMapping("/{attrGroupId}/attr/relation")
    //@RequiresPermissions("product:attrgroup:list")
    public R listByAttrGroupId(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list = attrGroupService.listByAttrGroupId(attrGroupId);
        return R.ok().put("data", list);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,@PathVariable("catId") Long catId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        return R.ok();
    }


    @RequestMapping("/attr/relation")
    //@RequiresPermissions("product:attrgroup:relation")
    public R addRelations(@RequestBody AttrAttrgroupRelationVo[] relationEntities){
        relationService.addByAttrGroupRelations(relationEntities);
        return R.ok();
    }

    @RequestMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:relation:delete")
    public R delRelations(@RequestBody AttrAttrgroupRelationVo[] relationEntities){
        relationService.delByAttrGroupRelations(relationEntities);
        return R.ok();
    }

    /**
     * 根据分类id得到所有属性分组以及分组对应所有基本属性规格参数
     * @param catelogId
     * @return
     */
    @RequestMapping("/{catelogId}/withattr")
    //@RequiresPermissions("product:attrgroup:relation")
    public R withattr(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupEntity> list = attrGroupService.withattr(catelogId);
        return R.ok().put("data",list);
    }
}
