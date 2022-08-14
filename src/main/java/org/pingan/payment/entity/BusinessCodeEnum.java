package org.pingan.payment.entity;

public enum BusinessCodeEnum {

    /**
     * 支付微服务，内部错误逻辑返回码定义
     */
    BUSI_PAY_1000(1000, "支付已成功，请勿重新支付"),
    BUSI_PAY_1001(1001, "支付请求处理中，请稍后重试"),

    /**
     * 支付渠道错误码封装(2000开头,根据业务扩展)
     */
    BUSI_CHANNEL_FAIL(2000, "支付宝报文组装错误"),

    /**
     * 通知类异常
     */
    BUSI_NOTIFY_FAIL(3000, "通知账单信息已存在，不需要重复通知");

    private Integer code;
    private String desc;

    BusinessCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据编码获取枚举类型
     *
     * @return
     */
    public static BusinessCodeEnum getByCode(Integer code) {
        if (code == null) return null;
        BusinessCodeEnum[] values = BusinessCodeEnum.values();
        for (BusinessCodeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
