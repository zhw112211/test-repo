package com.imooc.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BestPayConfig {

    @Autowired
    private WxAccountConfig wxAccountConfig;

    @Autowired
    private AlipayAccountConfig alipayAccountConfig;


    //微信支付
    @Bean
    public WxPayConfig wxPayConfig() {
        //微信支付
        WxPayConfig wxPayConfig = new WxPayConfig();
        // appid  一个应用可以有多种支付方式
        wxPayConfig.setAppId(wxAccountConfig.getAppId());
        //商户ID
        wxPayConfig.setMchId(wxAccountConfig.getMchId());
        //密钥
        wxPayConfig.setMchKey(wxAccountConfig.getMchKey());
        //接受异步通知的地址
        wxPayConfig.setNotifyUrl(wxAccountConfig.getNotifyUrl());
        wxPayConfig.setReturnUrl(wxAccountConfig.getReturnUrl());
        return wxPayConfig;
    }

    //支付宝支付
    @Bean
    public AliPayConfig aliPayConfig(){
        AliPayConfig aliPayConfig = new AliPayConfig();
        aliPayConfig.setAppId(alipayAccountConfig.getAppId());
        //私钥
        aliPayConfig.setPrivateKey(alipayAccountConfig.getPrivateKey());
        //支付宝公钥
        aliPayConfig.setAliPayPublicKey(aliPayConfig.getAliPayPublicKey());
        //回调地址
        aliPayConfig.setNotifyUrl(alipayAccountConfig.getNotifyUrl());
        //支付后 支付宝跳转的地址
        aliPayConfig.setReturnUrl(alipayAccountConfig.getReturnUrl());
        return aliPayConfig;
    }


    //最好的支付 bestPayService
    @Bean
    public BestPayService bestPayService(AliPayConfig aliPayConfig,WxPayConfig wxPayConfig){
        BestPayServiceImpl bestPayService = new BestPayServiceImpl();
        bestPayService.setWxPayConfig(wxPayConfig);
        bestPayService.setAliPayConfig(aliPayConfig);
        return bestPayService;
    }





}
