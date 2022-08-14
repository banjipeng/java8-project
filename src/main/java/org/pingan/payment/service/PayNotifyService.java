package org.pingan.payment.service;

import org.pingan.payment.entity.ResponseResult;
import org.pingan.payment.entity.dto.AliPayReceiveDTO;

public interface PayNotifyService {

    /**
     * 支付宝支付结果通知回调
     * 该接口处理渠道支付结果，记录支付通知报文，处理支付订单状态，以及向接入方同步支付结果
     */
    String aliPayReceive(AliPayReceiveDTO aliPayReceiveDTO);
}
