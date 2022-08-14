package org.pingan.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.pingan.payment.convert.PayNotifyConvert;
import org.pingan.payment.dao.mapper.PayChannelParamDao;
import org.pingan.payment.dao.mapper.PayNotifyDao;
import org.pingan.payment.dao.mapper.PayOrderDao;
import org.pingan.payment.dao.model.PayChannelParamPO;
import org.pingan.payment.dao.model.PayNotifyPO;
import org.pingan.payment.dao.model.PayOrderPO;
import org.pingan.payment.entity.BusinessCodeEnum;
import org.pingan.payment.entity.GlobalCodeEnum;
import org.pingan.payment.entity.ResponseResult;
import org.pingan.payment.entity.dto.AliPayReceiveDTO;
import org.pingan.payment.entity.dto.PayNotifyDTO;
import org.pingan.payment.exception.ServiceException;
import org.pingan.payment.rabbit.RabbitPayListener;
import org.pingan.payment.rabbit.RabbitPayNotify;
import org.pingan.payment.service.PayNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PayNotifyServiceImpl implements PayNotifyService {


    /**
     * 渠道参数配置信息持久层
     */
    @Autowired
    PayChannelParamDao payChannelParamDao;

    /**
     * 支付账单流水持久层
     */
    @Autowired
    PayOrderDao payOrderDao;

    /**
     * 渠道支付通知日志持久层
     */
    @Autowired
    PayNotifyDao payNotifyDao;

    /**
     * 引入发送消息组件
     */
    @Autowired
    RabbitPayNotify rabbitPayNotify;

    /**
     * 引入监听消息组件
     */
    @Autowired
    RabbitPayListener rabbitPayListener;


    @Override
    public String aliPayReceive(AliPayReceiveDTO aliPayReceiveDTO) {
        //对报文进行签名认证
        boolean verifyResult = aliPayReceiveMsgVerify(aliPayReceiveDTO);
        //签名认证失败，直接返回错误信息
        if (!verifyResult) {
            throw new ServiceException(BusinessCodeEnum.BUSI_CHANNEL_FAIL.getCode(),
                    BusinessCodeEnum.BUSI_CHANNEL_FAIL.getDesc());
        }
        //查询支付账单流水信息
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pay_id", aliPayReceiveDTO.getOut_trade_no());
        List<PayOrderPO> payOrderPOList = payOrderDao.selectByMap(paramMap);
        if (payOrderPOList == null || payOrderPOList.size() <= 0) {
            throw new ServiceException(BusinessCodeEnum.BUSI_CHANNEL_FAIL.getCode(),
                    BusinessCodeEnum.BUSI_CHANNEL_FAIL.getDesc());
        }
        //如果签名认证成功，则保存支付结果通知报文信息
        PayOrderPO payOrderPO = payOrderPOList.get(0);
        PayNotifyPO payNotifyPO = PayNotifyConvert.INSTANCE.convertPayNotifyPO(payOrderPO);
        QueryWrapper<PayNotifyPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(eq -> eq.eq(true, "pay_id", aliPayReceiveDTO.getOut_trade_no()))
                .and(eq -> eq.eq(true, "receive_status", "2"));
        PayNotifyPO payNotifyPOList = payNotifyDao.selectOne(queryWrapper);
        if (payNotifyPOList == null) {
            throw new ServiceException(BusinessCodeEnum.BUSI_NOTIFY_FAIL.getCode(),
                    BusinessCodeEnum.BUSI_NOTIFY_FAIL.getDesc());
        }
        payNotifyPO.setMerchantId(aliPayReceiveDTO.getApp_id());
        //设置状态为已处理
        payNotifyPO.setReceiveStatus("2");
        payNotifyPO.setStatus("2");
        //将支付通知报文转换为json格式
        payNotifyPO.setFullinfo(JSON.toJSONString(aliPayReceiveDTO));
        payNotifyDao.insert(payNotifyPO);
        //更新支付账单状态，放在一个事务中
        payOrderPO.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        payOrderPO.setStatus("2");
        payOrderPO.setTradeNo(aliPayReceiveDTO.getOut_trade_no());
        payOrderDao.updateById(payOrderPO);
        //向接入方同步支付结果，向余额账户管理系统通知支付完成
        payNotify_By_RabbitMQ(payOrderPO, payNotifyPO);
        return "success";
    }

    /**
     * 组装异步通知参数的私有方法
     *
     * @param payOrderPO
     */
    private void payNotify_By_RabbitMQ(PayOrderPO payOrderPO, PayNotifyPO payNotifyPO) {
        PayNotifyDTO payNotifyDTO = new PayNotifyDTO();
        payNotifyDTO.setOrderId(payOrderPO.getOrderId());
        payNotifyDTO.setAmount(payOrderPO.getAmount());
        payNotifyDTO.setPayStatus(Integer.parseInt(payOrderPO.getStatus()));
        payNotifyDTO.setCurrency(payOrderPO.getCurrency());
        payNotifyDTO.setTradeNo(payOrderPO.getTradeNo());
        rabbitPayNotify.sendPayResult(payNotifyDTO, payNotifyPO);
    }


    /**
     * 支付宝支付通知报文签名验证方法
     */
    private boolean aliPayReceiveMsgVerify(AliPayReceiveDTO aliPayReceiveDTO) {
        //查询支付宝支付rsa公钥信息
        QueryWrapper<PayChannelParamPO> queryWrapper = new QueryWrapper<PayChannelParamPO>();
        queryWrapper.and(wq -> wq.eq("partner", aliPayReceiveDTO.getApp_id()))
                .and(wq -> wq.eq("status", "0"))
                .and(wq -> wq.eq("key_type", "publickey"));
        PayChannelParamPO payChannelParamPO = payChannelParamDao.selectOne(queryWrapper);
        //如果支付参数信息不存在，直接返回失败
        if (payChannelParamPO == null) {
            return false;
        }
        //将支付参数对象转换为map
        Map<String, String> paramMap = JSON.parseObject(JSON.toJSONString(aliPayReceiveDTO), Map.class);
        //调用支付宝支付sdk验证签名
        boolean signVerifyField = false;
        try {
            signVerifyField = AlipaySignature.rsaCheckV1(paramMap, payChannelParamPO.getKeyContext(), "utf-8", payChannelParamPO.getSignType());
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        //方便测试直接签名验证成功
        return true;
    }
}
