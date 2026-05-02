package com.qiao.demo.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiao.demo.inventory.model.Product;

public interface ProductService extends IService<Product> {
    
    // 带有事务和并发控制的入库操作
    boolean stockIn(Integer productId, int quantity);

    // 带有事务和并发控制的入库操作（指定消息ID，供 MQ Consumer 进行幂等性保护）
    boolean stockIn(Integer productId, int quantity, String messageId);

    // 带有事务和并发控制的出库操作（使用默认客户名）
    boolean stockOut(Integer productId, int quantity);
    
    // 带有事务和并发控制的出库操作（指定客户名，供 MQ Consumer 回调）
    boolean stockOut(Integer productId, int quantity, String customerName);

    // 带有事务和并发控制的出库操作（指定客户名和消息ID，供 MQ Consumer 进行幂等性保护）
    boolean stockOut(Integer productId, int quantity, String customerName, String messageId);
    
    // 动态库存预警相关方法
    void calculateDynamicReorderPoint(Integer productId);
    void batchCalculateReorderPoints();
    void updateDemandStatistics(Integer productId);
    void batchUpdateDemandStatistics();
    boolean isStockBelowReorderPoint(Integer productId);
    boolean checkAndTriggerAlert(Integer productId, Integer deductQuantity);
}