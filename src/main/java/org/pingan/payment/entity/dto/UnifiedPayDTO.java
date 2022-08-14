package org.pingan.payment.entity.dto;

import lombok.Data;
import org.pingan.payment.validator.EnumValue;

import javax.validation.constraints.NotNull;

@Data
public class UnifiedPayDTO {

    /**
     * 接入方应用id
     */
    @NotNull(message = "应用id不能为空")
    private String appId;
    /**
     * 接入方支付账单id，必须是接入方系统唯一
     */
    @NotNull(message = "支付账单id不能为空")
    private String orderId;
    /**
     * 交易类型，标识具体的业务类型
     */
    @EnumValue(strValues = {"topup"})
    private String tradeType;
    /**
     * 支付渠道。0-微信支付，1-支付宝支付
     */
    @EnumValue(intValues = {0, 1})
    private Integer channel;
    /**
     * 支付产品定义，区分具体的渠道支付产品
     */
    private String payType;
    /**
     * 支付金额
     */
    private Integer amount;
    /**
     * 支付币种
     */
    @EnumValue(strValues = {"CNY"})
    private String currency;
    /**
     * 接入方系统唯一标识用户身份id
     */
    @NotNull(message = "用户id不能为空")
    private String userId;
    /**
     * 产品标题
     */
    @NotNull(message = "商品标题不能为空")
    private String subject;
    /**
     * 商品描述信息
     */
    private String body;
    /**
     * 支付扩展信息。针对某些支付渠道的特殊请求参数的补充
     */
    private Object extraInfo;
    /**
     * 异步支付结果通知地址
     */
    @NotNull(message = "支付通知地址不能为空")
    private String notifyUrl;
    /**
     * 同步支付结果跳转地址(支付成功后同步跳转回接入方系统界面的url)
     */
    private String returnUrl;

}
