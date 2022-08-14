package org.pingan.payment.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.pingan.payment.dao.model.PayNotifyPO;
import org.pingan.payment.dao.model.PayOrderPO;

@Mapper
public interface PayNotifyConvert {

    PayNotifyConvert INSTANCE = Mappers.getMapper(PayNotifyConvert.class);

    @Mappings({})
    PayNotifyPO convertPayNotifyPO(PayOrderPO payOrderPO);
}
