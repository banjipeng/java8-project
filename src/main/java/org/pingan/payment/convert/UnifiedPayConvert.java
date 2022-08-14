package org.pingan.payment.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.pingan.payment.dao.model.PayOrderPO;
import org.pingan.payment.entity.bo.UnifiedPayBO;
import org.pingan.payment.entity.dto.UnifiedPayDTO;

@Mapper
public interface UnifiedPayConvert {

    UnifiedPayConvert INSTANCE = Mappers.getMapper(UnifiedPayConvert.class);

    /**
     * 生成支付账单输出数据对象
     */
    @Mappings({
            @Mapping(target = "extraInfo", ignore = true)
    })
    UnifiedPayBO convertUnifiedPayBO(UnifiedPayDTO unifiedPayDTO);

    /**
     * 支付请求参数对象到支付账单持久层实体类对象
     */
    @Mappings({})
    PayOrderPO convertPayOrderPO(UnifiedPayDTO unifiedPayDTO);
}
