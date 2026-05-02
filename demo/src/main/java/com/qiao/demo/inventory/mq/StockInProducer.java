package com.qiao.demo.inventory.mq;

import com.qiao.demo.inventory.config.RabbitConfig;
import com.qiao.demo.inventory.dto.StockInDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StockInProducer {

    private static final Logger log = LoggerFactory.getLogger(StockInProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(StockInDTO dto) {
        dto.setMessageId(UUID.randomUUID().toString());
        log.info("发送入库消息: productId={}, quantity={}, messageId={}",
                dto.getProductId(), dto.getQuantity(), dto.getMessageId());
        rabbitTemplate.convertAndSend(RabbitConfig.STOCK_IN_EXCHANGE,
                RabbitConfig.STOCK_IN_ROUTING_KEY, dto);
    }
}
