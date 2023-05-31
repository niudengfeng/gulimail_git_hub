package com.atguigu.gulimail.order.service;

import com.alipay.api.response.AlipayFundTransCommonQueryResponse;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gulimail.order.vo.AliPayRequest;
import com.atguigu.gulimail.order.vo.AliPayResultQueryRequest;
import com.atguigu.gulimail.order.vo.AliPayTransQueryRequest;
import com.atguigu.gulimail.order.vo.AliPayTransRequest;

public interface AliPayService {

    AlipayTradeAppPayResponse aliPay(AliPayRequest aliPayRequest);

    AlipayTradeQueryResponse aliPayQuery(AliPayResultQueryRequest request);

    AlipayFundTransUniTransferResponse trans(AliPayTransRequest transParam);

    AlipayFundTransCommonQueryResponse transQuery(AliPayTransQueryRequest transQueryRequest);
}
