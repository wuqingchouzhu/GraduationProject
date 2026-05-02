package com.qiao.demo.inventory.mq;

import com.qiao.demo.inventory.config.RabbitConfig;
import com.qiao.demo.inventory.dto.StockOutDTO;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class StockOutDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockOutDlqConsumer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = RabbitConfig.DLQ_QUEUE)
    public void onDlqMessage(@Payload StockOutDTO dto, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.error("[DLQ] 出库消息进入死信队列: productId={}, quantity={}, customerName={}, 时间={}",
                dto.getProductId(), dto.getQuantity(), dto.getCustomerName(), now);

        try {
            String sql = "INSERT INTO failed_messages (message_type, product_id, quantity, customer_name, fail_reason, fail_time, message_id) " +
                    "VALUES ('STOCK_OUT', ?, ?, ?, '系统异常进入DLQ', ?, ?)";
            jdbcTemplate.update(sql, dto.getProductId(), dto.getQuantity(),
                    dto.getCustomerName(), now, dto.getMessageId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[DLQ] 记录失败消息时出错", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("[DLQ] NACK失败", ex);
            }
        }
    }
}
