-- ==========================================
-- 死信队列失败消息记录表 DDL
-- 执行此SQL创建表后，DLQ消费者才能正常入库
-- ==========================================
CREATE TABLE IF NOT EXISTS `failed_messages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `message_type` VARCHAR(50) NOT NULL COMMENT '消息类型: STOCK_IN / STOCK_OUT',
    `product_id` INT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL COMMENT '数量',
    `customer_name` VARCHAR(100) DEFAULT '默认出库客户' COMMENT '客户名称',
    `fail_reason` VARCHAR(500) DEFAULT '系统异常进入DLQ' COMMENT '失败原因',
    `fail_time` DATETIME NOT NULL COMMENT '失败时间',
    `message_id` VARCHAR(36) DEFAULT NULL COMMENT '消息ID（用于幂等性排查）',
    INDEX `idx_fail_time` (`fail_time`),
    INDEX `idx_message_type` (`message_type`),
    INDEX `idx_failed_messages_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败消息记录表（DLQ）';
