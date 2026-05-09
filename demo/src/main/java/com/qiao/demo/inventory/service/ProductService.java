package com.qiao.demo.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiao.demo.inventory.model.Product;

public interface ProductService extends IService<Product> {
    
    boolean stockIn(Integer productId, int quantity);

    boolean stockIn(Integer productId, int quantity, String messageId);

    boolean stockOut(Integer productId, int quantity);
    
    boolean stockOut(Integer productId, int quantity, String customerName);

    boolean stockOut(Integer productId, int quantity, String customerName, String messageId);
    
    void calculateDynamicReorderPoint(Integer productId);
    void batchCalculateReorderPoints();
    void updateDemandStatistics(Integer productId);
    void batchUpdateDemandStatistics();
    boolean isStockBelowReorderPoint(Integer productId);
    boolean checkAndTriggerAlert(Integer productId, Integer deductQuantity);
}