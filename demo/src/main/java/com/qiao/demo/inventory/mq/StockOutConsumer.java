package com.qiao.demo.inventory.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qiao.demo.inventory.config.RabbitConfig;
import com.qiao.demo.inventory.dao.ProductMapper;
import com.qiao.demo.inventory.dto.StockOutDTO;
import com.qiao.demo.inventory.model.Product;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class StockOutConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockOutConsumer.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public void onMessage(StockOutDTO dto, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            processStockOut(dto);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("出库消费失败: productId={}, quantity={}, 原因={}", dto.getProductId(), dto.getQuantity(), e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }

    private void processStockOut(StockOutDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            log.warn("出库失败：商品不存在, id={}", dto.getProductId());
            return;
        }

        if (product.getStockLevel() < dto.getQuantity()) {
            log.warn("出库失败：库存不足, productId={}, 当前={}, 需要={}", dto.getProductId(), product.getStockLevel(), dto.getQuantity());
            return;
        }

        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, dto.getProductId())
                .ge(Product::getStockLevel, dto.getQuantity())
                .eq(Product::getVersion, product.getVersion())
                .setSql("stock_level = stock_level - " + dto.getQuantity());

        int rows = productMapper.update(null, updateWrapper);
        if (rows == 0) {
            log.warn("出库失败：乐观锁冲突或库存不足, productId={}", dto.getProductId());
            return;
        }

        Product updated = productMapper.selectById(dto.getProductId());
        if (updated != null) {
            checkAndUpdateAlertStatus(updated);
        }

        String sql = "INSERT INTO stock_out_record (product_id, quantity, customer_name, create_time) VALUES (?, ?, ?, NOW())";
        jdbcTemplate.update(sql, dto.getProductId(), dto.getQuantity(),
                dto.getCustomerName() != null ? dto.getCustomerName() : "默认出库客户");

        log.info("出库成功: productId={}, quantity={}", dto.getProductId(), dto.getQuantity());
    }

    private void checkAndUpdateAlertStatus(Product product) {
        if (product.getReorderPoint() == null) {
            return;
        }
        BigDecimal currentStock = BigDecimal.valueOf(product.getStockLevel());
        BigDecimal reorderPoint = product.getReorderPoint();

        if (currentStock.compareTo(reorderPoint) < 0) {
            if (product.getAlertStatus() == 0) {
                product.setAlertStatus(1);
                productMapper.updateById(product);
                log.info("库存预警：商品{} 库存{} 低于预警线{}", product.getProductName(), currentStock, reorderPoint);
            }
        } else {
            if (product.getAlertStatus() == 1) {
                product.setAlertStatus(0);
                productMapper.updateById(product);
                log.info("商品{} 库存已恢复至预警线以上", product.getProductName());
            }
        }
    }
}
