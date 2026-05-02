package com.qiao.demo.inventory.mq;

import com.qiao.demo.inventory.common.BusinessException;
import com.qiao.demo.inventory.config.RabbitConfig;
import com.qiao.demo.inventory.dto.StockInDTO;
import com.qiao.demo.inventory.service.ProductService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class StockInConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockInConsumer.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = RabbitConfig.STOCK_IN_QUEUE)
    public void onMessage(StockInDTO dto, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            if (dto.getMessageId() != null) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM stock_in_record WHERE message_id = ?",
                        Integer.class, dto.getMessageId());
                if (count != null && count > 0) {
                    log.warn("入库消息重复，已跳过: productId={}, messageId={}",
                            dto.getProductId(), dto.getMessageId());
                    channel.basicAck(deliveryTag, false);
                    return;
                }
            }

            productService.stockIn(dto.getProductId(), dto.getQuantity(), dto.getMessageId());
            channel.basicAck(deliveryTag, false);
            log.info("入库消费成功: productId={}, quantity={}",
                    dto.getProductId(), dto.getQuantity());
        } catch (BusinessException e) {
            log.warn("入库业务失败(已ACK不重试): productId={}, quantity={}, 原因={}",
                    dto.getProductId(), dto.getQuantity(), e.getMessage());
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception ex) {
                log.error("ACK失败", ex);
            }
        } catch (Exception e) {
            log.error("入库系统异常(进入DLQ): productId={}, quantity={}, 原因={}",
                    dto.getProductId(), dto.getQuantity(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }
}
