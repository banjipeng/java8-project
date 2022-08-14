package org.pingan.payment.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.pingan.payment.dao.model.PayOrderPO;
import org.springframework.stereotype.Repository;

@Repository
public interface PayOrderDao extends BaseMapper<PayOrderPO> {
}
