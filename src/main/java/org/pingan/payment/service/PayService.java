package org.pingan.payment.service;

import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;

public interface PayService {

    /**
     * 定义统一支付接口
     */
    UnifiedPayBO unifiedPay(UnifiedPayDTO unifiedPayDTO);
}
