package org.pingan.payment.rabbit;

import org.pingan.payment.entity.GlobalCodeEnum;
import org.pingan.payment.entity.ResponseResult;
import org.pingan.payment.service.impl.PayNotifyServiceImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class RabbitPayListener {

    @Autowired
    RabbitTemplate rabbitTemplate;

    public ResponseResult<String> receiveAccountResult() {
        return rabbitTemplate.receiveAndConvert("balanceaccount-payment",
                new ParameterizedTypeReference<ResponseResult<String>>() {});
    }
}
