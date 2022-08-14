package org.pingan.payment.service;


import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;

public interface PayChannelService {

    /**
     * 渠道支付业务层接口
     */
    UnifiedPayBO pay(UnifiedPayDTO unifiedPayDTO);
}
