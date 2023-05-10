package com.atguigu.gulimail.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.RedisConstants;
import com.atguigu.gulimail.product.dao.CategoryBrandRelationDao;
import com.atguigu.gulimail.product.entity.CateGory3Vo;
import com.atguigu.gulimail.product.entity.IndexCatelogGoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimail.product.dao.CategoryDao;
import com.atguigu.gulimail.product.entity.CategoryEntity;
import com.atguigu.gulimail.product.service.CategoryService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    CategoryBrandRelationDao categoryBrandRelationDao;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );
        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithChildren() {
        //1.先得到商品菜单所有数据
        //2.再所有数据的基础上得到所有一级菜单的数据（共性:parentCid均为0）
        //3.在所有一级菜单的基础上得到其所有的子菜单集合（写一个循环遍历方法）
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        List<CategoryEntity> collect = categoryEntities.stream()
                .filter(menu -> menu.getParentCid().equals(0L)) //过滤掉父ID不是0的
                .map(menu -> { //给一级菜单得到所有的子菜单集合
                    menu.setChildrenList(this.getChildrens(menu,categoryEntities));
                    return menu;
                })
                .sorted(Comparator.comparing(CategoryEntity::getSort,Comparator.nullsFirst(Integer::compareTo))/*.reversed()*/) //reversed倒序
                .collect(Collectors.toList());//转化成List集合返回
        return collect;
    }

    @Override
    @CacheEvict(value = "category",allEntries = true)
    public void removeBatchByIds(List<Long> catIds) {
        //TODO 删除之前先有判断逻辑
        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 修改分类数据，需要更新redis，这里直接采取删除缓存数据，下次查询去DB，然后重新放入redis
     * 以下两种方式选其一，一般用@CacheEvict(value = "category",allEntries = true)
     * @param category
     */
    @Override
    //代表删除category分区下的所有缓存数据
    @CacheEvict(value = "category",allEntries = true)
    //组合命令，删除指定key
   /* @Caching(evict = {
            @CacheEvict(value = {"category"},key = "'categoryLevelOne'"),
            @CacheEvict(value = {"category"},key = "'categoryLevelThree'")
    })*/
    public void updateByIdAndRelation(CategoryEntity category) {
        this.baseMapper.updateById(category);
        if (!StringUtils.isEmpty(category.getName())){
            Map map = new HashMap();
            map.put("cateLogId",category.getCatId());
            map.put("cateLogName",category.getName());
            categoryBrandRelationDao.updateByCatelogId(map);
        }
    }

    @Override
    @Cacheable(value = "category",key = "#root.methodName")
    public List<CategoryEntity> categoryLevelOne() {
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cat_level",1);
        queryWrapper.eq("show_status",1);
        queryWrapper.gt("product_count",0);
        queryWrapper.orderByAsc("sort");
        List<CategoryEntity> categoryEntities = this.getBaseMapper().selectList(queryWrapper);
        return categoryEntities;
    }

    /**
     * @Cacheable 用法
     *  1.每一个需要缓存的数据，我们都来指定要放到哪个名字的缓存，value=指定分区  便于后面的分区下缓存删除
     *  2.加了该注解的方法：代表当前方法的返回值需要缓存到redis，下次查询，如果缓存中有，直接返回，没有就查询数据库，然后放入缓存中
     *  3.默认行为
     *      a.如果缓存中有，方法不用执行调用
     *      b.key默认自动生成，缓存的名字:SimpleKey[]
     *      c.缓存的value值默认使用jdK序列化后的
     *      d.默认ttl -1  永不失效
     *    自定义行为：
     *    a.我们需要指定key名：key=spel表达式，可以参考官方文档：https://docs.spring.io/spring-framework/docs/5.1.12.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     *    b.指定过期时间
     *    c.缓存的value是json格式
     *
     * @return
     */
    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map categoryLevelThree() {
        List<CategoryEntity> ones = categoryLevelOne();
        Map<Long,List<IndexCatelogGoryVo>> cateGory3FromDb = getCateGory3FromDb(ones);
        return cateGory3FromDb;
    }

    /**
     * 根据一级分类ID，查询二级分类以及三级分类
     * @return
     */
    public Map<Long,List<IndexCatelogGoryVo>> getCateGory3FromDb(List<CategoryEntity> ones) {
        if (CollectionUtils.isEmpty(ones)){
            return new HashMap();
        }
        //先得到所有一级分类ID
        List<Long> oneIds = ones.stream().map(CategoryEntity::getCatId).collect(Collectors.toList());
        //拿到所有二级分类
        List<CategoryEntity> twos = getCateChildrens(oneIds);
        //二级分类不为null的情况，拿到所有三级分类
        List<CategoryEntity> threes;
        if (!CollectionUtils.isEmpty(twos)){
            List<Long> twoIds = twos.stream().map(CategoryEntity::getCatId).collect(Collectors.toList());
            threes = getCateChildrens(twoIds);
        } else {
            threes = new ArrayList<>();
        }

        Map<Long,List<IndexCatelogGoryVo>> resultMap = new HashMap();
        for (int i = 0; i < ones.size(); i++) {
            CategoryEntity o = ones.get(i);//一级分类
            if (CollectionUtils.isEmpty(twos)){
                resultMap.put(o.getCatId(),new ArrayList<IndexCatelogGoryVo>());
                continue;
            }
            //拿到当前一级分类下的所有二级分类
            List<IndexCatelogGoryVo> needsTwo = twos.stream()
                    .filter(t -> t.getParentCid().equals(o.getCatId()))//过滤出当前分类下的二级
                    .map(m->{
                        IndexCatelogGoryVo vo2 = new IndexCatelogGoryVo();
                        vo2.setName(m.getName());
                        vo2.setId(m.getCatId().toString());
                        vo2.setCatalog1Id(o.getCatId().toString());
                        if (CollectionUtils.isEmpty(threes)){
                            vo2.setCatalog3List(new ArrayList());
                        }else {
                            List<CateGory3Vo> needsThree = threes.stream()
                                    .filter(f -> f.getParentCid().equals(m.getCatId()))
                                    .map(m3->{
                                        CateGory3Vo cateGory3Vo = new CateGory3Vo();
                                        cateGory3Vo.setCatalog2Id(m.getCatId().toString());
                                        cateGory3Vo.setName(m3.getName());
                                        cateGory3Vo.setId(m3.getCatId().toString());
                                        return cateGory3Vo;
                                    })
                                    .collect(Collectors.toList());
                            vo2.setCatalog3List(needsThree);
                        }
                        return vo2;
                    })
                    .collect(Collectors.toList());
            resultMap.put(o.getCatId(),needsTwo);
        }
        return resultMap;
    }


    /**
     * 根据cateId集合得到所有子类分类对象
     * @param oneIds
     * @return
     */
    public List<CategoryEntity> getCateChildrens(List<Long> oneIds){
        QueryWrapper<CategoryEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("parent_cid",oneIds);
        queryWrapper.eq("show_status",1);
        queryWrapper.gt("product_count",0);
        queryWrapper.orderByAsc("sort");
        List<CategoryEntity> twos = this.baseMapper.selectList(queryWrapper);
        return twos;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity categoryEntity,List<CategoryEntity> list){
        List<CategoryEntity> collect = list.stream()
                .filter(cate -> cate.getParentCid().equals(categoryEntity.getCatId()) )//在所有数据的基础上得到父菜单是当前菜单的数据
                .map(menu -> {
                    menu.setChildrenList(this.getChildrens(menu, list));
                    return menu;
                })
                .sorted(Comparator.comparing(CategoryEntity::getSort, Comparator.nullsFirst(Integer::compareTo)))
                .collect(Collectors.toList());
        return collect;
    }

}