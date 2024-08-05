package com.imooc.service.impl;

import com.google.gson.Gson;
import com.imooc.enums.PayPlatformEnum;
import com.imooc.mapper.PayInfoMapper;
import com.imooc.pojo.PayInfo;
import com.imooc.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class PayServiceImpl implements IPayService {


    private static final String QUEUE_PAY_NOTIFY = "payNotify";
    @Autowired
    private BestPayService bestpayService;

    //依赖注入paymapper
    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;


    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        // 写入数据库中
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);
        payInfoMapper.insertSelective(payInfo);

        PayRequest payRequest = new PayRequest();
        //支付名称
        payRequest.setOrderName("8849462-最好的支付sdk");
        //支付订单号
        payRequest.setOrderId(orderId);
        //支付金额
        payRequest.setOrderAmount(amount.doubleValue());
        //支付方式
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        PayResponse response = bestpayService.pay(payRequest);
        log.info("response={}", response);
        return response;
    }


    @Override
    public String asyncNotify(String notifyData) {
        //1.校验签名
        PayResponse payResponse = bestpayService.asyncNotify(notifyData);

        log.info("异步通知  Response={}", payResponse);

        //2.校验金额(从数据库中查询订单信息)
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if (payInfo == null) {
            //todo 告警 使用阿里短信
            throw new RuntimeException("通过orderNo查询到的结果为null");
        }

        //判断支付状态  如果订单支付状态不是已支付
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            //compareTo 结果 0 1 -1
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
                //todo 告警 使用阿里短信
                throw new RuntimeException("异步通知中的金额和数据库中不一致,orderNo = " + payResponse.getOrderId());
            }
            //3.修改订单状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            //交易流水号  由支付平台产生
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());

            // 更新时间  交由mysql进行管理

            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }


        //TODO 发送MQ消息 由pay发送 mall项目接受
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY,new Gson().toJson(payInfo));


        //4.告诉微信不要在进行通知了

        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            return "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            return "success";
        }
        throw new RuntimeException("异步通知中错误的支付平台");
    }

    /*
        通过订单号查询订单状态
     */

    @Override
    public PayInfo queryByOrderId(String orderId) {
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
