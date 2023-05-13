package com.atguigu.gulimail.search.controller;

import com.atguigu.gulimail.search.service.MailSearchService;
import com.atguigu.gulimail.search.vo.SearchParam;
import com.atguigu.gulimail.search.vo.SearchResultResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@Api(tags = "ES查询")
public class IndexController {

    @Autowired
    private MailSearchService searchService;

    /**
     * 商品的检索查询接口
     * @return
     */
    @GetMapping({"/list.html"})
    @ApiOperation("ES查询")
    public String index(SearchParam searchParam, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();//获取请求URL
        searchParam.setQueryString(queryString);
        SearchResultResponse result = searchService.searchEs(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
