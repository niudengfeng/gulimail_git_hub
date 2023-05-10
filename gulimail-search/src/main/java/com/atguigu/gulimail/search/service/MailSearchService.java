package com.atguigu.gulimail.search.service;

import com.atguigu.gulimail.search.vo.SearchParam;
import com.atguigu.gulimail.search.vo.SearchResultResponse;

public interface MailSearchService {
    SearchResultResponse searchEs(SearchParam searchParam);
}
