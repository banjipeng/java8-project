package org.pingan.payment.service.impl;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pingan.payment.dao.mapper.PayNotifyDao;
import org.pingan.payment.dao.model.PayNotifyPO;
import org.pingan.payment.entity.dto.PayNotifyDTO;
import org.pingan.payment.rabbit.RabbitPayNotify;
import org.pingan.payment.service.PayNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Service
public class ExecutorRepeatNotifyService {

    //重复执行的任务
    private final RabbitPayNotify rabbitPayNotify;
    //重复执行的配置信息
    //重复执行的次数
    private final Integer count;
    //重复执行的持续时间
    private final Integer duration;
    //执行请求的参数
    private final PayNotifyDTO payNotifyDTO;
    //重复执行任务线程池
    private final ExecutorService task;
    //执行次数的结果
    private final AtomicInteger res;

    private final PayNotifyPO payNotifyPO;

    private final AtomicInteger notifyCount = new AtomicInteger();

    @Autowired
    PayNotifyDao payNotifyDao;


    public ExecutorRepeatNotifyService(RabbitPayNotify rabbitPayNotify, Integer count,
                                       Integer duration, PayNotifyDTO payNotifyDTO,
                                       PayNotifyPO payNotifyPO) {
        this.rabbitPayNotify = rabbitPayNotify;
        this.count = count;
        this.duration = duration;
        this.payNotifyDTO = payNotifyDTO;
        this.res = new AtomicInteger(count);
        this.payNotifyPO = payNotifyPO;
        this.task = (ExecutorService) Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(() -> {
                    if (res.get() == 0) {
                        this.terminated();
                    }
                    try {
                        this.execute();
                        this.terminated();
                    } catch (Exception e) {
                        //更新通知信息表
                        //更新通知次数
                        payNotifyPO.setNotifyCount(notifyCount.incrementAndGet());
                        //更新通知时间
                        payNotifyPO.setNotifyTime(new Timestamp(System.currentTimeMillis()));
                        //更新最后一次更新时间
                        payNotifyPO.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                        payNotifyDao.updateById(payNotifyPO);
                    }

                }, 0, duration, TimeUnit.HOURS);
    }

    //任务的执行
    public void execute() {
        rabbitPayNotify.sendPayResult(payNotifyDTO, payNotifyPO);
        res.getAndDecrement();
    }

    //任务的销毁工作
    public void terminated() {
        task.shutdown();
    }

}
