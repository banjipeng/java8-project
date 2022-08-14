package org.pingan.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pingan.payment.convert.UnifiedPayConvert;
import org.pingan.payment.entity.BusinessCodeEnum;
import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;
import org.pingan.payment.exception.ServiceException;
import org.pingan.payment.service.PayChannelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AliPayServiceImpl implements PayChannelService {

    /**
     * 支付网关接口
     */
    @Value("${channel.alipay.payUrl}")
    private String payUrl;
    /**
     * 支付宝应用id
     */
    @Value("${channel.alipay.appId}")
    private String appId;
    /**
     * 支付宝应用密钥
     */
    @Value("${channel.alipay.privateKey}")
    private String privateKey;

    @Value("${channel.alipay.publicKey}")
    private String publicKey;

    private String format = "json";

    private String charset = "UTF-8";

    private String signType = "RSA2";

    @Override
    public UnifiedPayBO pay(UnifiedPayDTO unifiedPayDTO) {
        //获得初始化的alipayclient
        DefaultAlipayClient alipayClient = new DefaultAlipayClient(payUrl, appId, privateKey, format, charset, publicKey, signType);
        //创建api对应的request
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        //在公共参数中设置同步跳转地址和异步支付结果通知地址
        alipayRequest.setReturnUrl(unifiedPayDTO.getReturnUrl());
        alipayRequest.setNotifyUrl(unifiedPayDTO.getNotifyUrl());
        //填充业务参数，（支付产品的请求参数要求）
        BizContent bizContent = BizContent.builder().out_trade_no(String.valueOf(unifiedPayDTO.getOrderId()))
                .product_code("FAST_INSTANT_TRADE_PAY")
                .total_amount(Double.valueOf(unifiedPayDTO.getAmount()) / 100)
                .subject(unifiedPayDTO.getSubject())
                .body(unifiedPayDTO.getBody())
                .passback_params("merchantBizType%" + unifiedPayDTO.getTradeType())
                .build();
        alipayRequest.setBizContent(JSON.toJSONString(bizContent));
        //用户支付宝网页跳转携带的form表单数据
        String form = "";
        try {
            //调用sdk生成支付请求参数
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            //将支付渠道错误封装为系统可识别的异常码
            throw new ServiceException(BusinessCodeEnum.BUSI_CHANNEL_FAIL.getCode(),
                    BusinessCodeEnum.BUSI_CHANNEL_FAIL.getDesc(), e);
        }
        UnifiedPayBO unifiedPayBO = UnifiedPayConvert.INSTANCE.convertUnifiedPayBO(unifiedPayDTO);
        //设置需要接入方处理的form表单
        unifiedPayBO.setExtraInfo(form);
        unifiedPayBO.setTradeNo(unifiedPayDTO.getOrderId());
        //设置为待支付状态
        unifiedPayBO.setPayStatus(0);
        return unifiedPayBO;
    }

    /**
     * 内部类用于封装支付宝请求参数中的业务参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class BizContent {
        private String out_trade_no;
        private String product_code;
        private Double total_amount;
        private String subject;
        private String body;
        private String passback_params;
    }
}
