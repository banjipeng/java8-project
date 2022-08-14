package org.pingan.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PayChannelServiceFactory {

    @Autowired
    private PayChannelService aliPayServiceImpl;

    /**
     * 根据渠道代码获取具体的渠道业务层处理类
     */
    public PayChannelService createPayChannelService(int channelName){
        switch (channelName){
            case 1:
                return aliPayServiceImpl;
            default:
                return null;
        }
    }
}
