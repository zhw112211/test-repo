package com.imooc.service;

import com.imooc.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {


    /*
        创建发起支付
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /*
        接受微信发起的异步通知
     */
    String asyncNotify(String notifyData);

    PayInfo queryByOrderId(String orderId);
}
