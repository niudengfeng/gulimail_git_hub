package com.atguigu.gulimail.product.service.impl;

import com.atguigu.common.dto.EsAttrModel;
import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.product.Enums.SpuStatusEnum;
import com.atguigu.gulimail.product.entity.*;
import com.atguigu.gulimail.product.service.*;
import com.atguigu.gulimail.product.service.feign.FeignCouponService;
import com.atguigu.gulimail.product.service.feign.FeignElasticSearchService;
import com.atguigu.gulimail.product.service.feign.FeignWareService;
import com.atguigu.gulimail.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService attrValueService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private FeignCouponService feignCouponService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private FeignWareService feignWareService;
    @Autowired
    private FeignElasticSearchService feignElasticSearchService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String status = String.valueOf(params.get("status"));
        if (!status.equals("null") && !StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",Integer.valueOf(status));
        }
        String brandId = String.valueOf(params.get("brandId")==null?"0":params.get("brandId"));
        if (!brandId.equals("null") && !StringUtils.isEmpty(brandId) && Long.parseLong(brandId)!=0L){
            wrapper.eq("brand_id",Long.parseLong(brandId));
        }
        String catelogId = String.valueOf(params.get("catelogId")==null?"0":params.get("catelogId"));
        if (!catelogId.equals("null") && !StringUtils.isEmpty(catelogId) && Long.parseLong(catelogId)!=0L){
            wrapper.eq("catalog_id",Long.parseLong(catelogId));
        }
        String key = String.valueOf(params.get("key"));
        if (!key.equals("null") && !StringUtils.isEmpty(key)){
            wrapper.and(w->{
                w.eq("id",key).or().like("spu_name",key);
            });
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(SaveSpuInfoBean spuInfoBean) {
        log.info("保存spu信息开始===============》");
        //1.保存spu基本信息：pms_spu_info
        SpuInfoEntity spu = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoBean,spu);
        spu.setCreateTime(new Date());
        spu.setUpdateTime(new Date());
        this.save(spu);
        log.info("保存spu基本信息===============》pms_spu_info"+spu);
        //2.保存spu的图片描述：pms_spu_info_desc
        List<String> decript = spuInfoBean.getDecript();
        if (decript!=null && decript.size()>0){
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spu.getId());
            spuInfoDescEntity.setDecript(decript.stream().filter(d->!StringUtils.isEmpty(d)).collect(Collectors.joining(",")));
            spuInfoDescService.save(spuInfoDescEntity);
            log.info("保存spu图片描述===============》pms_spu_info_desc"+spuInfoDescEntity);
        }
        //3.保存spu的图片集：pms_spu_images
        List<String> imagesList = spuInfoBean.getImages();
        if (imagesList!=null && imagesList.size()>0){
            List<SpuImagesEntity> spuImages = imagesList.stream().map(image -> {
                SpuImagesEntity images = new SpuImagesEntity();
                images.setSpuId(spu.getId());
                images.setImgUrl(image);
                return images;
            }).filter(f->!StringUtils.isEmpty(f.getImgUrl())).collect(Collectors.toList());
            spuImagesService.saveBatch(spuImages);
            log.info("批量保存spu图片集===============》pms_spu_images"+spuImages);
        }
        //4.保存spu的规格参数：pms_product_attr_value
        List<BaseAttrVo> baseAttrs = spuInfoBean.getBaseAttrs();
        if (baseAttrs!=null && baseAttrs.size()>0){
            List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attrVo -> {
                ProductAttrValueEntity attr = new ProductAttrValueEntity();
                attr.setSpuId(spu.getId());
                Long attrId = attrVo.getAttrId();
                AttrEntity byId = attrService.getById(attrId);
                attr.setAttrId(attrId);
                attr.setAttrName(byId.getAttrName());
                attr.setAttrValue(attrVo.getAttrValues());
                attr.setQuickShow(attrVo.getShowDesc());
                return attr;
            }).filter(s->s.getAttrId()!=null).collect(Collectors.toList());
            attrValueService.saveBatch(collect);
            log.info("批量保存spu的规格参数===============》pms_product_attr_value"+collect);
        }
        //5.保存spu的积分信息gulimail_sms->sms_spu_bounds
        Bounds bounds = spuInfoBean.getBounds();
        bounds.setSpuId(spu.getId());
        R r = feignCouponService.saveBound(bounds);
        if ((Integer) r.get("code")==0){
            log.info("远程调用coupon服务保存spu的积分信息成功============>"+bounds);
        }
        //6.保存spu对应的sku信息
        List<Skus> skus = spuInfoBean.getSkus();
        if (skus!=null && skus.size()>0){
            skus.forEach(sku->{
                String skuDefaultImg = "";
                List<Images> images = sku.getImages();
                if (images!=null && images.size()>0){
                    for (Images image : images){
                        if (image.getDefaultImg() == 1){
                            skuDefaultImg = image.getImgUrl();
                        }
                    }
                }
                //a.sku的基本信息pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setSpuId(spu.getId());
                skuInfoEntity.setBrandId(spu.getBrandId());
                skuInfoEntity.setCatalogId(spu.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(skuDefaultImg);
                skuInfoService.save(skuInfoEntity);
                log.info("sku的基本信息===============》pms_sku_info"+skuInfoEntity);
                Long skuId = skuInfoEntity.getSkuId();
                //b.sku的图片信息pms_sku_images
                List<Images> skuImages = sku.getImages();
                if (skuImages!=null && skuImages.size()>0){
                    List<SkuImagesEntity> collect = skuImages.stream().map(skuImage -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setDefaultImg(skuImage.getDefaultImg());
                        skuImagesEntity.setImgUrl(skuImage.getImgUrl());
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
                    skuImagesService.saveBatch(collect);
                    log.info("批量保存sku的图片信息===============》pms_sku_images"+collect);
                }
                //c.sku的销售属性信息pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                if (attr!=null && attr.size()>0){
                    List<SkuSaleAttrValueEntity> collect = attr.stream().map(a -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        skuSaleAttrValueEntity.setAttrId(a.getAttrId());
                        skuSaleAttrValueEntity.setAttrName(a.getAttrName());
                        skuSaleAttrValueEntity.setAttrValue(a.getAttrValue());
                        skuSaleAttrValueEntity.setSkuId(skuId);
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
                    skuSaleAttrValueService.saveBatch(collect);
                    log.info("批量保存sku的销售属性信息===============》pms_sku_sale_attr_value"+collect);
                }

                //d.sku的优惠满减等信息gulimail_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                BigDecimal fullPrice = sku.getFullPrice();
                Integer fullCount = sku.getFullCount();
                if (fullCount > 0){
                    SkuLadderVo skuLadderVo = new SkuLadderVo();
                    BeanUtils.copyProperties(sku,skuLadderVo);
                    skuLadderVo.setSkuId(skuId);
                    skuLadderVo.setAddOther(sku.getCountStatus());
                    R r1 = feignCouponService.saveLadder(skuLadderVo);
                    if ((Integer) r1.get("code")==0){
                        log.info("远程调用coupon服务保存商品阶梯价格成功===========>"+skuLadderVo);
                    }
                }

                //sms_sku_full_reduction
                if (fullPrice.compareTo(BigDecimal.ZERO)>0){
                    SkuFullReductionVo skuFullReductionVo = new SkuFullReductionVo();
                    BeanUtils.copyProperties(sku,skuFullReductionVo);
                    skuFullReductionVo.setAddOther(sku.getCountStatus());
                    skuFullReductionVo.setSkuId(skuId);
                    R r2 = feignCouponService.saveFullReduction(skuFullReductionVo);
                    if ((Integer) r2.get("code")==0){
                        log.info("远程调用coupon服务保存商品满减信息成功===========>"+skuFullReductionVo);
                    }
                }

                //sms_member_price
                List<MemberPrice> memberPriceList = sku.getMemberPrice();
                if (memberPriceList!=null && memberPriceList.size()>0){
                    List<MemberPriceVo> memberPriceVos = memberPriceList.stream().map(item -> {
                        Long memberLevelId = item.getId();
                        String memberLevelName = item.getName();
                        BigDecimal price = item.getPrice();
                        MemberPriceVo memberPriceVo = new MemberPriceVo();
                        memberPriceVo.setMemberLevelId(memberLevelId);
                        memberPriceVo.setAddOther(sku.getCountStatus());
                        memberPriceVo.setSkuId(skuId);
                        memberPriceVo.setMemberLevelName(memberLevelName);
                        memberPriceVo.setMemberPrice(price);
                        return memberPriceVo;
                    }).filter(f->f.getMemberPrice().compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
                    R r3 = feignCouponService.saveMemberPrice(memberPriceVos);
                    if ((Integer) r3.get("code")==0){
                        log.info("远程调用coupon服务保存会员价格成功===========>"+memberPriceVos);
                    }
                }
            });
        }
        log.info("保存spu信息结束《===============");
    }

    @Override
    @Transactional
    public void up(Long spuId) {
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        if (skus == null || skus.size()<=0){
            return;
        }
        //2 远程调用库存服务ware检查是否有库存hasStock
        Map hasStockMap = new HashMap();
        try{
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R r = feignWareService.hasStock(skuIds);
            if (r.getCode() == 0){
                //调用成功
                hasStockMap = (Map) r.get("data");
            }
        }catch (Exception e){
            log.error("远程调用库存服务异常,{}",e);
            e.printStackTrace();
        }
        Map finalHasStockMap = hasStockMap;

        //查询可以用来被检索的规格属性
        Set<Long> sets = new HashSet<>();//用来存储可以被用来检索的规格属性ID：attrIds
        List<EsAttrModel> attrs = new ArrayList<>();
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.getDataBySpuId(spuId);
        if (productAttrValueEntities!=null && productAttrValueEntities.size()>0){
            List<Long> attrIds = productAttrValueEntities.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
            if (attrIds!=null && attrIds.size()>0){
                //去pms_attr表查询当前属性ids下的可以被用来检索的
                List<Long> attrIdsSearch = attrService.getSearchDataByAttrIds(attrIds);//可以被用来检索的属性id
                if (attrIdsSearch!=null && attrIdsSearch.size()>0){
                    sets.addAll(attrIdsSearch);
                }
            }
        }
        if (sets!=null && sets.size()>0){
            attrs = productAttrValueEntities.stream()
                    .filter(attr -> sets.contains(attr.getAttrId()))//过滤掉不在set集合中的属性id
                    .map(attr -> {
                        EsAttrModel esAttrModel = new EsAttrModel();
                        esAttrModel.setAttrId(attr.getAttrId());
                        esAttrModel.setAttrName(attr.getAttrName());
                        esAttrModel.setAttrValue(attr.getAttrValue());
                        return esAttrModel;
                    }).collect(Collectors.toList());
        }

        List<EsAttrModel> finalAttrs = attrs;
        List<SkuEsModel> collect = skus.stream().map(sku -> {
            //1.封装SkuEsModel
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku,skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            skuEsModel.setHotScore(0L);//默认热度评分为0
            //判断当前sku是否有库存
            if (finalHasStockMap !=null && finalHasStockMap.size()>0){
                skuEsModel.setHasStock( finalHasStockMap.get(sku.getSkuId()) == null ? false : (Boolean) finalHasStockMap.get(sku.getSkuId()));
            }
            BrandEntity brand = brandService.getById(sku.getBrandId());
            if (brand!=null){
                skuEsModel.setBrandName(brand.getName());
                skuEsModel.setBrandImg(brand.getLogo());
            }
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            if (categoryEntity!=null){
                skuEsModel.setCatalogName(categoryEntity.getName());
            }
            skuEsModel.setAttrs(finalAttrs);
            return skuEsModel;
        }).collect(Collectors.toList());

        //4.远程调用ES服务保存SkuEsModel
        try{
            R r = feignElasticSearchService.saveSkus(collect);
            if (r.getCode() == 0){
                //5.修改商品状态为上架成功
                SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
                spuInfoEntity.setId(spuId);
                spuInfoEntity.setUpdateTime(new Date());
                spuInfoEntity.setPublishStatus(SpuStatusEnum.UP.getCode());
                this.baseMapper.updateById(spuInfoEntity);
            }else {
                log.error("远程调用ES服务存储SKU报错，{}",r);
            }
        }catch (Exception e){
            log.error("远程调用ES服务存储SKU异常，{}",e);
            e.printStackTrace();
        }
    }
}