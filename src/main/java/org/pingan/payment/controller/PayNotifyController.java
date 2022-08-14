package org.pingan.payment.controller;


import lombok.extern.slf4j.Slf4j;
import org.pingan.payment.entity.ResponseResult;
import org.pingan.payment.entity.dto.AliPayReceiveDTO;
import org.pingan.payment.service.PayNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/notify")
public class PayNotifyController {

    @Autowired
    PayNotifyService payNotifyServiceImpl;

    /**
     * 定义支付宝的异步支付结果通知的接口
     * 请求对象根据支付宝异步支付结果通知的接口文档规范进行定义
     */
    @PostMapping("/aliPayReceive")
    public ResponseResult<String> aliPayReceive(@RequestBody AliPayReceiveDTO aliPayReceiveDTO) {
        return ResponseResult.OK(payNotifyServiceImpl.aliPayReceive(aliPayReceiveDTO));
    }
}
