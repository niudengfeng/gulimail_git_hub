package com.atguigu.gulimail.order.config;

import com.alipay.api.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 沙箱账号
 * 商家信息
 * 商户账号rscril9308@sandbox.com
 * 登录密码111111
 * 商户PID2088721011478322
 * 账户余额1000000.00
 * 买家信息
 * 买家账号itnhju5769@sandbox.com
 * 登录密码111111
 * 支付密码111111
 * 用户UID2088722011478334
 * 用户名称itnhju5769
 * 证件类型IDENTITY_CARD
 * 证件账号789712191500133114
 * 账户余额1000000.00
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "ali.pay")
public class AlipayClientConfig {

    private String serverUrl;
    private String appid;
    private String privateKey;
    private String publicKey;

    /**
     * 非对称加密玩法：应用有自己的私钥和公钥 支付宝也有私钥和公钥，我们用自己的私钥加密 支付宝用应用的公钥解密，然后支付宝用自己的私钥加密 我们用支付宝的公钥解密
     * @return
     * @throws AlipayApiException
     */
    @Bean(name = "alipayClient")
    public AlipayClient alipayClient() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(serverUrl);
        //设置应用Id
        alipayConfig.setAppId(appid);
        //设置应用私钥
        alipayConfig.setPrivateKey(privateKey);
        //设置请求格式，固定值json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(publicKey);
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        //构造client
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        return alipayClient;
    }
}
