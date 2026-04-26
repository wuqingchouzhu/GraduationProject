package com.qiao.demo.inventory.mq;

import com.qiao.demo.inventory.config.RabbitConfig;
import com.qiao.demo.inventory.dto.StockOutDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockOutProducer {

    private static final Logger log = LoggerFactory.getLogger(StockOutProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(StockOutDTO dto) {
        log.info("发送出库消息: productId={}, quantity={}", dto.getProductId(), dto.getQuantity());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, dto);
    }
}
