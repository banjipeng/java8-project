package org.pingan.payment.dao.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("pay_channel_param")
public class PayChannelParamPO {

    private Integer id;
    /**
     * 具体的渠道参数账号
     */
    private String partner;
    /**
     * 报文签名类型
     */
    private String signType;
    /**
     * 密钥类型
     */
    private String keyType;
    /**
     * 证书文本内容
     */
    private String keyContext;
    /**
     * 证书到期时间
     */
    private Timestamp expireTime;
    /**
     * 状态
     */
    private String status;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 备注信息
     */
    private String remark;
}
