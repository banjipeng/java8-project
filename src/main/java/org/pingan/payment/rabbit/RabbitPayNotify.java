package org.pingan.payment.rabbit;

import com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent;
import lombok.extern.slf4j.Slf4j;
import org.pingan.payment.dao.mapper.PayNotifyDao;
import org.pingan.payment.dao.model.PayNotifyPO;
import org.pingan.payment.entity.dto.PayNotifyDTO;
import org.pingan.payment.service.impl.ExecutorRepeatNotifyService;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RabbitPayNotify {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PayNotifyDao payNotifyDao;

    /**
     * 将支付结果同步至余额账户系统和
     *
     * @param payNotifyDTO
     */
    public void sendPayResult(final PayNotifyDTO payNotifyDTO, final PayNotifyPO payNotifyPO) {
        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        MessageProperties props = new MessageProperties();
        Message message = messageConverter.toMessage(payNotifyDTO, props);
        try {
            rabbitTemplate.send(message);
            //更新数据库信息
            payNotifyPO.setNotifyTime(new Timestamp(System.currentTimeMillis()));
            payNotifyPO.setNotifyCount(payNotifyPO.getNotifyCount() + 1);
            payNotifyPO.setReceiveStatus("3");
            payNotifyPO.setVerify(0);
            payNotifyDao.updateById(payNotifyPO);
        } catch (Exception e) {
            //如果消息发送失败，进行重新发送
            log.info("支付结果发送失败" + "-->" + e.getMessage());
            //重复5次，持续24小时,启用调度线程持续执行
            ExecutorRepeatNotifyService.builder()
                    .rabbitPayNotify(this)
                    .payNotifyDTO(payNotifyDTO)
                    .payNotifyPO(payNotifyPO)
                    .count(5)
                    .duration(5)
                    .build();
        }
    }
}
