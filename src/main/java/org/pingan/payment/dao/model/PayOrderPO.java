package org.pingan.payment.dao.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("pay_order")
public class PayOrderPO {

    private Integer id;
    /**
     * 业务方账单号(业务方系统唯一)
     */
    private String orderId;
    /**
     * 业务方交易类型
     */
    private String tradeType;
    /**
     * 支付订单金额
     */
    private Integer amount;
    /**
     * 支付币种
     */
    private String currency;
    /**
     * 支付订单状态
     */
    private String status;
    /**
     * 支付渠道编码
     */
    private String channel;
    /**
     * 渠道支付方式
     */
    private String payType;
    /**
     * 支付微服务账单流水号
     */
    private String payId;
    /**
     * 第三方渠道流水号
     */
    private String tradeNo;
    /**
     * 支付账单创建时间
     */
    private Timestamp createTime;
    /**
     * 支付账单更新时间
     */
    private Timestamp updateTime;

    private String userId;

}
