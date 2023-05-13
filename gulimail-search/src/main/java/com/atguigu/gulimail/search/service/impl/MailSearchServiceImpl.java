package com.atguigu.gulimail.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.dto.SkuEsModel;
import com.atguigu.common.utils.EsConstants;
import com.atguigu.common.utils.R;
import com.atguigu.gulimail.search.service.MailSearchService;
import com.atguigu.gulimail.search.service.feign.ProdcutFeign;
import com.atguigu.gulimail.search.vo.SearchParam;
import com.atguigu.gulimail.search.vo.SearchResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MailSearchServiceImpl implements MailSearchService {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ProdcutFeign prodcutFeign;

    /**
     * 执行search.gulimail.com首页搜索功能
     * @param searchParam
     * @return
     */
    @Override
    public SearchResultResponse searchEs(SearchParam searchParam) {
        SearchResultResponse searchResultResponse = new SearchResultResponse();
        //1.构建ES查询语句
        SearchRequest searchRequest = builderSearch(searchParam);
        //2.执行语句
        try {
            log.info("开始执行es查询语句");
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            log.info("得到查询结果："+response.toString());
            //3.分析响应结果，封装成SearchResultResponse
            searchResultResponse = builderResponse(response,searchParam);
        } catch (Exception e) {
            log.error("查询ES报错",e.getCause());
            e.printStackTrace();
        }finally {
            return searchResultResponse;
        }
    }

    /**
     * 构建ES查询语句
     * @param searchParam
     * @return
     */
    private SearchRequest builderSearch(SearchParam searchParam) {
        SearchSourceBuilder source = SearchSourceBuilder.searchSource();
        /////////////////////////////////////构建query条件开始/////////////////////////////////////////////////////////////
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        /**TODO 关键字模糊查询商品名称*/
        if (! StringUtils.isEmpty(searchParam.getKeyword())){
            MatchQueryBuilder skuTitle = QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword());
            boolQueryBuilder.must(skuTitle);
        }
        /**TODO 三级分类id*/
        if (searchParam.getCatalog3Id() != null){
            TermQueryBuilder catalogId = QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id());
            boolQueryBuilder.filter(catalogId);
        }
        /**TODO 品牌多选*/
        if (!CollectionUtils.isEmpty(searchParam.getBrandId())){
            TermsQueryBuilder brandId = QueryBuilders.termsQuery("brandId", searchParam.getBrandId());
            boolQueryBuilder.filter(brandId);
        }
        /**TODO 是否有库存*/
        if (searchParam.getHasStock() != null){
            TermQueryBuilder hasStock = QueryBuilders.termQuery("hasStock", searchParam.getHasStock()==1);
            boolQueryBuilder.filter(hasStock);
        }
        /**TODO skuPrice范围搜索 格式x_x */
        if (! StringUtils.isEmpty(searchParam.getSkuPrice())){
            String[] s = searchParam.getSkuPrice().trim().split("_");
            List<String> collect = Arrays.stream(s).filter(f -> !StringUtils.isEmpty(f)).collect(Collectors.toList());
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            if (collect.size()==2){
                skuPrice.gte(new BigDecimal(collect.get(0))).lte(new BigDecimal(collect.get(1)));
            }else if (collect.size()==1){
                if (searchParam.getSkuPrice().startsWith("_")) {
                    skuPrice.lte(new BigDecimal(collect.get(0)));
                }else{
                    skuPrice.gte(new BigDecimal(collect.get(0)));
                }
            }
            boolQueryBuilder.filter(skuPrice);
        }
        /**TODO 属性多选 约定格式attrId_attrValue1:attrValue2 */
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())) {
            for (String attr : searchParam.getAttrs()) {
                if (!StringUtils.isEmpty(attr)){
                    String[] split = attr.split("_");
                    long attrId = Long.parseLong(split[0]);//attrId
                    String[] attrValues = split[1].split(":");//attrValues
                    BoolQueryBuilder must = QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("attrs.attrId", attrId))
                            .must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                    NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", must, ScoreMode.None);
                    boolQueryBuilder.filter(attrs);
                }
            }
        }
        source.query(boolQueryBuilder);
        /////////////////////////////////////构建query条件结束/////////////////////////////////////////////////////////////

        /**TODO 准备构建分页、排序、高亮*/
        //from 需要计算  假如 1页 form 0  size 10 2页 from 10 size 10 3页 from 20 10
        //每页最大条数：10  得到规律第一页 0-9  第二页 10-19 第三页 20-29
        //每页最大条数：20  得到规律第一页 0-19  第二页 20-39 第三页 40-69
        //根据以上规律：得到from公式=(页码-1)*pageSize;
        source.from((searchParam.getPageNum()-1)*searchParam.getPageSize());
        source.size(searchParam.getPageSize());
        /**TODO 准备构建排序 约定格式 skuPrice_asc/desc  hotScore_asc/desc*/
        if (!StringUtils.isEmpty(searchParam.getSort())){
            String[] s = searchParam.getSort().split("_");
            String field = s[0];
            String sort = s[1];
            source.sort(field, SortOrder.fromString(sort));
        }
        /**TODO 准备构建高亮 这里只会在keyword不为null的情况才需要高亮*/
        if (! StringUtils.isEmpty(searchParam.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            source.highlighter(highlightBuilder);
        }

        String dsl = source.toString();
        log.info("得到ES的DSL语句："+dsl);
        /**TODO 准备构建分组聚合*/
        //TODO 分組邏輯
        //#1.根据品牌ID分组，显示品牌名称，图片
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandId_aggs");
        brandAgg.field("brandId").size(1000);
        brandAgg.subAggregation(AggregationBuilders.terms("brandName_aggs").field("brandName").size(1000));
        brandAgg.subAggregation(AggregationBuilders.terms("brandImg_aggs").field("brandImg").size(1000));
        source.aggregation(brandAgg);
        // #2.根据分类ID分组，显示分类名称
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogId_aggs").field("catalogId").size(1000);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalogName_aggs").field("catalogName").size(1000));
        source.aggregation(catalogAgg);
        // #3.属性ID分组,显示属性名称和属性值
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_aggs", "attrs");
        TermsAggregationBuilder attrIdAggs = AggregationBuilders.terms("attrId_aggs").field("attrs.attrId").size(1000);
        attrIdAggs.subAggregation(AggregationBuilders.terms("attrName_aggs").field("attrs.attrName").size(1000));
        attrIdAggs.subAggregation(AggregationBuilders.terms("attrValue_aggs").field("attrs.attrValue").size(1000));
        nested.subAggregation(attrIdAggs);
        source.aggregation(nested);
        dsl = source.toString();
        log.info("得到ES的DSL语句含分組："+dsl);
        SearchRequest request = new SearchRequest(new String[]{EsConstants.PRODUCT_INDEX},source);
        return request;
    }

    /**
     * 分析响应结果，封装成SearchResultResponse
     *
     * @param response
     * @param searchParam
     * @return
     */
    private SearchResultResponse builderResponse(SearchResponse response, SearchParam searchParam) {
        SearchResultResponse resultResponse = new SearchResultResponse();
        Aggregations aggregations = response.getAggregations();
        //1.得到所有屬性分組List<AttrVo>
        List<SearchResultResponse.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAggs = aggregations.get("attr_aggs");
        ParsedLongTerms attrIdAggs = attrAggs.getAggregations().get("attrId_aggs");
        for (Terms.Bucket bucket : attrIdAggs.getBuckets()) {
            SearchResultResponse.AttrVo attrVo = new SearchResultResponse.AttrVo();
            String attrId = bucket.getKeyAsString();
            //得到attrId
            attrVo.setAttrId(Long.valueOf(attrId));
            //獲取attrName 一個ID只會對應一個name
            try {
                ParsedStringTerms attrNameAggs = bucket.getAggregations().get("attrName_aggs");
                String keyAsString = attrNameAggs.getBuckets().get(0).getKeyAsString();
                attrVo.setAttrName(keyAsString);
            } catch (Exception e) {
                attrVo.setAttrName("");
                log.error("该属性下没有对应名称");
            }
            //獲取attrValue 一对多
            try {
                ParsedStringTerms attrValueAggs = bucket.getAggregations().get("attrValue_aggs");
                attrVo.setAttrValue(attrValueAggs.getBuckets().stream().map(m->m.getKeyAsString()).collect(Collectors.toList()));
                attrVos.add(attrVo);
            } catch (Exception e) {
                log.error("该属性下没有对应属性");
                attrVos.add(attrVo);
            }
        }
        resultResponse.setAttrs(attrVos);

        //2.获取所有分类
        List<SearchResultResponse.CategoryVo> categoryVos = new ArrayList<SearchResultResponse.CategoryVo>();
        ParsedLongTerms catalogIds = aggregations.get("catalogId_aggs");
        for (Terms.Bucket bucket : catalogIds.getBuckets()) {
            SearchResultResponse.CategoryVo categoryVo = new SearchResultResponse.CategoryVo();
            try {
                Long aLong = Long.valueOf(bucket.getKeyAsString());
                categoryVo.setCatalogId(aLong);
                ParsedStringTerms a = bucket.getAggregations().get("catalogName_aggs");
                String keyAsString = a.getBuckets().get(0).getKeyAsString();//一个分类ID只会对应一个分类名称
                categoryVo.setCatalogName(keyAsString);
                categoryVos.add(categoryVo);
            } catch (Exception e) {
                categoryVos.add(categoryVo);
                log.error("该分类下没有数据");
            }
        }
        resultResponse.setCategorys(categoryVos);

        //3.获取所有品牌
        List<SearchResultResponse.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandIdAggs = aggregations.get("brandId_aggs");
        for (Terms.Bucket bucket : brandIdAggs.getBuckets()) {
            SearchResultResponse.BrandVo brandVo = new SearchResultResponse.BrandVo();
            Long brandId = Long.valueOf(bucket.getKeyAsString());
            brandVo.setBrandId(brandId);
            ParsedStringTerms brandNameAggs = bucket.getAggregations().get("brandName_aggs");
            String keyAsString = brandNameAggs.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(keyAsString);
            ParsedStringTerms brandImg_aggs = bucket.getAggregations().get("brandImg_aggs");
            String brandImg = brandImg_aggs.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        resultResponse.setBrands(brandVos);

        //4.得到所有商品信息
        List<SkuEsModel> products = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        for (SearchHit hit : hits) {
            try {
                SkuEsModel skuEsModel = JSON.parseObject(hit.getSourceAsString(), SkuEsModel.class);
                if (! StringUtils.isEmpty(searchParam.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    Text[] fragments = skuTitle.getFragments();
                    skuEsModel.setSkuTitle(fragments[0].string());
                }
                products.add(skuEsModel);
            } catch (Exception e) {
                log.error("商品数据有问题"+e.getMessage());
            }
        }
        resultResponse.setProducts(products);

        //5.获取总记录数
        int value = (int) response.getHits().getTotalHits().value;
        resultResponse.setTotal(value);

        //6.获取总页数 需要计算 11条  每页显示5条  11对5取模 有余就加1  2+1=3页
        int i = value % searchParam.getPageSize() == 0 ? value / searchParam.getPageSize() : value / (searchParam.getPageSize()) + 1;
        resultResponse.setTotalPages(i);

        //7.返回当前页
        resultResponse.setPageNum(searchParam.getPageNum());

        //8.返回页码
        List<Integer> navs = new ArrayList<>();
        for (int j = 1; j <= resultResponse.getTotalPages() ; j++) {
            navs.add(j);
        }
        resultResponse.setNavs(navs);
        //9.返回面包屑导航
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())){
            List<String> attrs = searchParam.getAttrs();
            List<SearchResultResponse.NavVo> collect = attrs.stream().map(m -> {
                if (StringUtils.isEmpty(m) || m.endsWith("_") || m.split("_").length<2 || StringUtils.isEmpty(m.split("_")[0])){
                    return null;
                }
                SearchResultResponse.NavVo navVo = new SearchResultResponse.NavVo();
                String[] s = m.split("_");
                Long attrId = Long.valueOf(s[0].toString());
                navVo.setAttrId(attrId);
                //根据attrId去prodcut 查询attrName
                R info = prodcutFeign.info(attrId);
                if (info!=null && info.getCode().intValue() == 0){
                    Map<String, Object> attr = (Map<String, Object>) info.get("attr");
                    navVo.setAttrName(String.valueOf(attr.get("attrName")));
                }else {
                    navVo.setAttrName(s[0]);
                }
                String attrValues = s[1];
                navVo.setAttrValue(attrValues);

                String queryString = searchParam.getQueryString();
                String attrUrl = attrId + "_" + attrValues;
                String encode="";
                try {
                    encode = URLEncoder.encode(attrUrl, "UTF-8");
                    encode = encode.replace("+","%20");//浏览器空格替换
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                String replace = queryString.replace("&attrs="+encode, "");
                queryString = "http://search.gulimail.com/list.html?"+ replace;
                navVo.setBackUrl(queryString);
                return navVo;
            }).collect(Collectors.toList());
            resultResponse.setNavos(collect);
        }
        return resultResponse;
    }
}
