package org.pingan.payment.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.pingan.payment.convert.UnifiedPayConvert;
import org.pingan.payment.dao.mapper.PayOrderDao;
import org.pingan.payment.dao.model.PayOrderPO;
import org.pingan.payment.entity.BusinessCodeEnum;
import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;
import org.pingan.payment.exception.ServiceException;
import org.pingan.payment.service.PayChannelService;
import org.pingan.payment.service.PayChannelServiceFactory;
import org.pingan.payment.service.PayService;
import org.pingan.payment.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayServiceImpl implements PayService {

    /**
     * 定义redis分布式锁前缀
     */
    private final String redisLockPrefix = "pay-order&";

    /**
     * 引入redis分布式锁的依赖
     */
    @Autowired
    private RedisLockRegistry redisLockRegistry;

    /**
     * 支付账单持久层依赖
     */
    @Autowired
    private PayOrderDao payOrderDao;

    /**
     * 支付渠道处理工厂类依赖
     */
    @Autowired
    private PayChannelServiceFactory payChannelServiceFactory;


    /**
     * 统一支付接口的业务处理方法
     */
    @Override
    public UnifiedPayBO unifiedPay(UnifiedPayDTO unifiedPayDTO) {
        //返回的数据对象
        UnifiedPayBO unifiedPayBO = null;
        //创建redis分布式锁
        //支付防并发安全逻辑，通过前缀 + 业务接入方业务账单号获取redis分布式锁（同一笔账单,同一时刻只允许一个线程处理）
        Lock lock = redisLockRegistry.obtain(redisLockPrefix + unifiedPayDTO.getOrderId());
        //持有锁，等待时间为1s
        boolean isLock = false;
        try {
            isLock = lock.tryLock(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (isLock) {
            //数据库级别账单状态防重判断
            boolean isRepeatPayOrder = isSuccessPayOrder(unifiedPayDTO);
            if (isRepeatPayOrder) {
                throw new ServiceException(BusinessCodeEnum.BUSI_PAY_1000.getCode(),
                        BusinessCodeEnum.BUSI_PAY_1000.getDesc());
            }
            //支付账单入库
            String payId = this.payOrderSave(unifiedPayDTO);
            //获取具体的支付渠道服务类实例
            PayChannelService payChannelService = payChannelServiceFactory.createPayChannelService(unifiedPayDTO.getChannel());
            //调用渠道支付方法设置支付平台账单流水号
            unifiedPayDTO.setOrderId(payId);
            unifiedPayBO = payChannelService.pay(unifiedPayDTO);
            //释放分布式锁
            lock.unlock();
        } else {
            //如果持有锁，说明请求正在被处理,提示用户稍后重试
            throw new ServiceException(BusinessCodeEnum.BUSI_PAY_1001.getCode(),
                    BusinessCodeEnum.BUSI_PAY_1001.getDesc());
        }
        return unifiedPayBO;
    }

    /**
     * 支付账单入库方法
     */
    private String payOrderSave(UnifiedPayDTO unifiedPayDTO) {
        PayOrderPO payOrderPO = UnifiedPayConvert.INSTANCE.convertPayOrderPO(unifiedPayDTO);
        //设置支付状态为待支付
        payOrderPO.setStatus("0");
        //生成支付平台流水号
        String payId = createPayId();
        payOrderPO.setPayId(payId);
        payOrderPO.setUserId(unifiedPayDTO.getUserId());
        //订单创建时间
        payOrderPO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //账单更新时间
        payOrderPO.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        //账单入库操作
        payOrderDao.insert(payOrderPO);
        return payOrderPO.getPayId();
    }

    /**
     * 生成支付平台账单号
     */
    private String createPayId() {
        //获取10000~99999的随机数
        Integer random = new Random().nextInt(99999) % (99999 - 10000 + 1) + 10000;
        //时间戳 + 随机数
        String payId = DateUtils.getStringByFormat(new Date(), DateUtils.sf3) + String.valueOf(random);
        return payId;
    }

    /**
     * 数据库级别判断是否成功支付账单的私有方法
     */
    private boolean isSuccessPayOrder(UnifiedPayDTO unifiedPayDTO) {
        Map<String, Object> param = new HashMap<>();
        param.put("order_id", unifiedPayDTO.getOrderId());
        List<PayOrderPO> payOrderPOList = payOrderDao.selectByMap(param);
        if (payOrderPOList != null && payOrderPOList.size() > 0) {
            //判断支付账单中是否存在支付状态为成功的账单，若存在则不处理新的请求
            List<PayOrderPO> successPayOrderList = payOrderPOList.stream()
                    .filter(o -> "2".equals(o.getStatus()))
                    .collect(Collectors.toList());
            if (successPayOrderList != null && successPayOrderList.size() > 0) {
                return true;
            }
        }
        return false;
    }
}
