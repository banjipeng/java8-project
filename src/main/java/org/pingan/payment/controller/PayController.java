package org.pingan.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.pingan.payment.entity.ResponseResult;
import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;
import org.pingan.payment.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    PayService payServiceImpl;

    /**
     * 定义统一支付接口
     * 接收处理不同支付类型的支付请求，根据不同的支付渠道及方式
     * 实现相应的支付流程及参数适配
     */
    @PostMapping("/unifiedPay")
    public ResponseResult<UnifiedPayBO> unifiedPay(@RequestBody @Validated UnifiedPayDTO unifiedPayDTO) {
        return ResponseResult.OK(payServiceImpl.unifiedPay(unifiedPayDTO));
    }

}
