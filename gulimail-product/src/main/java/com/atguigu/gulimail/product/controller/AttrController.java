package com.atguigu.gulimail.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimail.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimail.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimail.product.service.CategoryService;
import com.atguigu.gulimail.product.service.ProductAttrValueService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimail.product.entity.AttrEntity;
import com.atguigu.gulimail.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品属性
 *
 * @author niudengfeng
 * @email 519507446@qq.com
 * @date 2021-03-23 16:20:19
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrAttrgroupRelationService relationService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 属性列表
     */
    @RequestMapping("/{type}/list/{catId}")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params
            ,@PathVariable("type") String type
            ,@PathVariable("catId") Long catId){
        params.put("type",type);
        params.put("catId",catId);
        PageUtils page = attrService.queryPageByType(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);
        return R.ok().put("page", page);
    }


    //    获取spu规格 /product/attr/base/listforspu/{spuId}
    @RequestMapping("/base/listforspu/{spuId}")
    public R listforspu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> dataBySpuId = productAttrValueService.getDataBySpuId(spuId);
        return R.ok().put("data",dataBySpuId);
    }

    //    修改商品规格/product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateBySpuId(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> entities){
        productAttrValueService.updateBySpuId(spuId,entities);
        return R.ok();
    }
    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
        AttrEntity attr = attrService.getById(attrId);
        Map columnMap = new HashMap();
        columnMap.put("attr_id",attr.getAttrId());
        List<AttrAttrgroupRelationEntity> list = relationService.listByMap(columnMap);
        if (list!=null && list.size()>0){
            attr.setAttrGroupId(list.get(0).getAttrGroupId());
        }
        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrEntity attr){
        if (attr.getCatelogId()!=null){
            CategoryEntity byId = categoryService.getById(attr.getCatelogId());
            if (byId!=null){
                attr.setCatelogName(byId.getName());
            }
        }
        attrService.save(attr);
        attrService.saveRelation(attr);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrEntity attr){
        if (attr.getCatelogId()!=null){
            CategoryEntity byId = categoryService.getById(attr.getCatelogId());
            if (byId!=null){
                attr.setCatelogName(byId.getName());
            }
        }
        attrService.updateById(attr);
        attrService.saveRelation(attr);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
