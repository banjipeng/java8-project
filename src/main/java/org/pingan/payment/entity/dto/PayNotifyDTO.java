package org.pingan.payment.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.pingan.payment.validator.EnumValue;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class PayNotifyDTO implements Serializable {

    /**
     * 商户支付账单号
     */
    @NotNull(message = "支付账单号不能为空")
    private String orderId;

    /**
     * 支付账单金额
     */
    private Integer amount;

    /**
     * 支付币种
     */
    private String currency;

    /**
     * 交易账单号
     */
    private String tradeNo;

    /**
     * 支付账单状态
     */
    @EnumValue(intValues = {2, 3}, message = "只接收支付状态为成功或失败的通知")
    private Integer payStatus;

}
