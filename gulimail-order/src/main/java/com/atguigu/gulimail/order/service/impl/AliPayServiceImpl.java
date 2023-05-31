package com.atguigu.gulimail.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.AlipayFundTransCommonQueryRequest;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayFundTransCommonQueryResponse;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gulimail.order.service.AliPayService;
import com.atguigu.gulimail.order.vo.AliPayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.atguigu.gulimail.order.vo.AliPayResultQueryRequest;
import com.atguigu.gulimail.order.vo.AliPayTransQueryRequest;
import com.atguigu.gulimail.order.vo.AliPayTransRequest;

@Slf4j
@Service
public class AliPayServiceImpl implements AliPayService {

    @Value("${ali.pay.returnUrl}")
    private String returnUrl;
    @Value("${ali.pay.notifyUrl}")
    private String notifyUrl;

    @Autowired
    @Qualifier("alipayClient")
    private AlipayClient alipayClient;

    private AlipayClient alipayCertClient;
    /**
     * 响应参数
     * 参数	                类型			            描述
     * code                 String                10000:代表接口调用成功
     * msg                  String                SUCCESS:代表接口调用成功
     * out_trade_no	        String              商户网站唯一订单号
     * trade_no	            String	            该交易在支付宝系统中
     * total_amount	        String	     该笔订单的资金总额，单位为人民币（元），取值范围为 0.01~100000000.00，精确到小数点后两位。
     * seller_id	        String	        收款支付宝账号对应的支付宝唯一用户号。
     * merchant_order_no	String	        商户原始订单号，最大长度限制32位
     * @param aliPayRequest
     * @return
     */
    //官方接口文档：https://opendocs.alipay.com/apis/009zio
    @Override
    public AlipayTradeAppPayResponse aliPay(AliPayRequest aliPayRequest) {
        try{
            log.info("调用支付宝下单接口参数："+aliPayRequest);
            AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
            //配置需要的公共请求参数
            //支付完成后，支付宝向APP发起异步通知的地址
            request.setNotifyUrl(notifyUrl);
            //支付完成后，我们想让页面跳转回APP的页面，配置returnUrl
            request.setReturnUrl(returnUrl);
            //封装业务参数
            AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
            model.setOutTradeNo(aliPayRequest.getOutTradeNo());
            model.setTotalAmount(aliPayRequest.getTotalAmount().toPlainString());
            model.setSubject(aliPayRequest.getSubject());
            request.setBizModel(model);

            AlipayTradeAppPayResponse response = null;

            log.info("支付宝下单接口请求参数："+JSON.toJSON(request).toString());
            response = alipayClient.sdkExecute(request);
            log.info("支付宝下单接口返回："+JSON.toJSON(response).toString()+",body:"+response.getBody());
            return response;
        }catch (Exception e){
            log.error("调用支付宝支付下单接口异常",e.getMessage());
            return null;
        }
    }

    /**
     * 支付宝下单结果查询接口
     * @param bizRequest
     * @return
     */
    //官方接口文档：https://opendocs.alipay.com/apis/009zir
    @Override
    public AlipayTradeQueryResponse aliPayQuery(AliPayResultQueryRequest bizRequest) {
        log.info("调用支付宝下单结果查询接口参数："+bizRequest);
        try {
            //封装查询参数
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setTradeNo(bizRequest.getOutTradeNo());
            request.setBizModel(model);
            log.info("支付宝下单结果查询接口请求参数："+JSON.toJSON(request).toString());
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            log.info("支付宝下单结果查询接口返回："+JSON.toJSON(response).toString()+",body:"+response.getBody());
            return response;
        } catch (AlipayApiException e) {
            log.error("支付宝下单结果查询接口异常",e.getMessage());
            return null;
        }
    }

    /**
     * 支付宝转账提现接口
     * 接口文档地址：https://opendocs.alipay.com/open/02byuo?pathHash=bcf7e750&ref=api&scene=ca56bca529e64125a2786703c6192d41
     * @param transParam
     *
     * 响应示例：
     *  {
     *     "alipay_fund_trans_uni_transfer_response": {
     *         "code": "10000",成功
     *         "msg": "Success",
     *         "out_biz_no": "201808080001",
     *         "order_id": "20190801110070000006380000250621",
     *         "pay_fund_order_id": "20190801110070001506380000251556",
     *         "status": "SUCCESS",
     *         "trans_date": "2019-08-21 00:00:00"
     *     },
     *     "sign": "ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE"
     * }
     */
    public AlipayFundTransUniTransferResponse trans(AliPayTransRequest transParam) {
        log.info("调用支付宝转账提现接口参数："+transParam);
        try{
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
            model.setOutBizNo(transParam.getOutBizNo());
            model.setTransAmount(transParam.getAmount().toPlainString());
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            Participant payeeInfo = new Participant();
            payeeInfo.setIdentityType("ALIPAY_OPEN_ID");
            payeeInfo.setIdentity(transParam.getOpenId());
            /*IdentityType参与方的标识类型，目前支持如下类型： 1、ALIPAY_USER_ID 支付宝的会员ID 2、ALIPAY_LOGON_ID：支付宝登录号，支持邮箱和手机号格式 3、ALIPAY_OPEN_ID：支付宝openid
            当如果identity_type为BANKCARD_NO需传递该参数
            BankcardExtInfo bankcardExtInfo = new BankcardExtInfo();
            bankcardExtInfo.setInstName("招商银行");
            bankcardExtInfo.setAccountType("1");
            payeeInfo.setBankcardExtInfo(bankcardExtInfo);*/
            model.setPayeeInfo(payeeInfo);
            model.setBizScene("DIRECT_TRANSFER");
            model.setRemark("转账提现");
            request.setBizModel(model);
            log.info("调用支付宝的转账提现接口，请求参数："+JSON.toJSON(request).toString());
            AlipayFundTransUniTransferResponse response = alipayCertClient.certificateExecute(request);
            log.info("支付宝转账提现接口响应："+JSON.toJSON(response).toString());
            return response;
        }catch (Exception e){
            log.error("调用支付宝转账提现接口异常",e.getMessage());
            return null;
        }
    }

    /**
     * 官方文档：https://opendocs.alipay.com/open/02byup?pathHash=0971c5bf&ref=api&scene=f9fece54d41f49cbbd00dc73655a01a4
     * 响应示例：
     * {
     *     "alipay_fund_trans_common_query_response": {
     *         "code": "10000",
     *         "msg": "Success",
     *         "order_id": "20190801110070000006380000250621",
     *         "pay_fund_order_id": "20190801110070001506380000251556",
     *         "out_biz_no": "201808080001",
     *         "trans_amount": 1,
     *         "status": "SUCCESS",
     *         "pay_date": "2013-01-01 08:08:08",
     *         "arrival_time_end": "2013-01-01 08:08:08",
     *         "order_fee": "0.02",
     *         "error_code": "PAYEE_CARD_INFO_ERROR",
     *         "fail_reason": "收款方银行卡信息有误",
     *         "sub_order_error_code": "MID_ACCOUNT_CARD_INFO_ERROR",
     *         "sub_order_fail_reason": "收款方银行卡信息有误",
     *         "sub_order_status": "FAIL"
     *     },
     *     "sign": "ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE"
     * }
     * 支付宝提现结果查询接口
     * @param transQueryRequest
     * @return
     */
    public AlipayFundTransCommonQueryResponse transQuery(AliPayTransQueryRequest transQueryRequest){
        log.info("调用支付宝提现结果查询接口参数："+transQueryRequest);
        try{
            AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
            AlipayFundTransCommonQueryModel model = new AlipayFundTransCommonQueryModel();
            model.setOutBizNo(transQueryRequest.getOutBizNo());
            model.setOrderId(transQueryRequest.getOrderId());
            model.setBizScene("DIRECT_TRANSFER");
            /**
             * setProductCode（）取值
             * STD_RED_PACKET：现金红包
             * TRANS_ACCOUNT_NO_PWD：单笔无密转账到支付宝账户
             * TRANS_BANKCARD_NO_PWD：单笔无密转账到银行卡
             */
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            request.setBizModel(model);
            AlipayFundTransCommonQueryResponse response = alipayClient.certificateExecute(request);
            log.info("支付宝转账提现接口响应："+JSON.toJSON(response).toString());
/*            if (response.isSuccess()) {
                System.out.println("调用成功");
            } else {
                System.out.println("调用失败");
            }*/
            return response;
        }catch (Exception e){
            log.error("调用支付宝提现结果查询接口异常",e.getMessage());
            return null;
        }
    }
}
