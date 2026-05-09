package com.qiao.demo.inventory.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiao.demo.inventory.common.BusinessException;
import com.qiao.demo.inventory.dao.ProductMapper;
import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean stockIn(Integer productId, int quantity) {
        return stockIn(productId, quantity, null);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean stockIn(Integer productId, int quantity, String messageId) {
        if (quantity <= 0) {
            throw new BusinessException("入库失败：入库数量必须大于0");
        }

        boolean updateSuccess = this.lambdaUpdate()
                .eq(Product::getId, productId)
                .setSql("stock_level = stock_level + " + quantity)
                .update();
        
        if (!updateSuccess) {
            Product currentProduct = this.getById(productId);
            if (currentProduct == null) {
                throw new BusinessException("入库失败：商品不存在，ID=" + productId);
            }
            throw new RuntimeException("并发冲突：商品数据已被修改，请刷新后重试！");
        }

        Product updatedProduct = this.getById(productId);
        if (updatedProduct != null) {
            checkAndUpdateAlertStatus(updatedProduct, 0);
        }

        String sql = "INSERT INTO stock_in_record (product_id, quantity, operator, create_time, message_id) VALUES (?, ?, ?, NOW(), ?)";
        jdbcTemplate.update(sql, productId, quantity, "系统管理员", messageId);

        return true;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean stockOut(Integer productId, int quantity) {
        return stockOut(productId, quantity, "默认出库客户", null);
    }
    
    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean stockOut(Integer productId, int quantity, String customerName) {
        return stockOut(productId, quantity, customerName, null);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ, rollbackFor = Exception.class)
    public boolean stockOut(Integer productId, int quantity, String customerName, String messageId) {
        if (quantity <= 0) {
            throw new BusinessException("出库失败：出库数量必须大于0");
        }
        
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("出库失败：商品不存在，ID=" + productId);
        }
        if (product.getStockLevel() < quantity) {
            throw new BusinessException("出库失败：库存不足！当前仅剩 " + product.getStockLevel() + " 件。");
        }
        
        product.setStockLevel(product.getStockLevel() - quantity);
        boolean updated = this.updateById(product);
        
        if (!updated) {
            throw new RuntimeException("并发冲突：商品数据已被修改，请刷新后重试！");
        }
        
        String sql = "INSERT INTO stock_out_record (product_id, quantity, customer_name, create_time, message_id) VALUES (?, ?, ?, NOW(), ?)";
        jdbcTemplate.update(sql, productId, quantity, 
                customerName != null ? customerName : "默认出库客户",
                messageId);
        
        checkAndUpdateAlertStatus(product, quantity);
        
        return true;
    }

    private void checkAndUpdateAlertStatus(Product product, Integer deductQuantity) {
        if (product == null || product.getReorderPoint() == null) {
            return;
        }
        
        java.math.BigDecimal currentStock = java.math.BigDecimal.valueOf(product.getStockLevel());
        java.math.BigDecimal reorderPoint = product.getReorderPoint();
        
        if (currentStock.compareTo(reorderPoint) < 0) {
            if (product.getAlertStatus() == 0) {
                product.setAlertStatus(1);
                this.updateById(product);
                
                sendAlertNotification(product, 
                    currentStock.doubleValue(), 
                    reorderPoint.doubleValue());
            }
        } else {
            if (product.getAlertStatus() == 1) {
                product.setAlertStatus(0);
                this.updateById(product);
                System.out.println("商品 " + product.getProductName() + " 库存已恢复至预警线以上，预警状态已重置。");
            }
        }
    }
    
    private void sendAlertNotification(Product product, double currentStock, double reorderPoint) {
        String message = String.format(
            "【库存预警】商品【%s】(ID:%d) 当前库存 %.2f 低于预警线 %.2f，请及时补货！",
            product.getProductName(), 
            product.getId(),
            currentStock, 
            reorderPoint
        );
        System.out.println("发送预警通知：" + message);
    }
    
    @Override
    public void calculateDynamicReorderPoint(Integer productId) {
        Product product = this.getById(productId);
        if (product == null || product.getIsManualReorder() == 1) {
            return;
        }
        
        java.math.BigDecimal avgDailyDemand = product.getAvgDailyDemand();
        java.math.BigDecimal demandStdDev = product.getDemandStdDev();
        Integer leadTime = product.getLeadTime();
        
        if (avgDailyDemand == null || demandStdDev == null || leadTime == null || leadTime <= 0) {
            return;
        }
        
        java.math.BigDecimal leadTimeDecimal = java.math.BigDecimal.valueOf(leadTime);
        double sqrtLeadTime = Math.sqrt(leadTime);
        java.math.BigDecimal sqrtLeadTimeDecimal = java.math.BigDecimal.valueOf(sqrtLeadTime);
        
        java.math.BigDecimal demandDuringLeadTime = avgDailyDemand.multiply(leadTimeDecimal);
        
        java.math.BigDecimal safetyStock = java.math.BigDecimal.valueOf(1.65)
                .multiply(demandStdDev)
                .multiply(sqrtLeadTimeDecimal);
        
        java.math.BigDecimal reorderPoint = demandDuringLeadTime.add(safetyStock)
                .setScale(4, RoundingMode.HALF_UP);
        
        product.setReorderPoint(reorderPoint);
        this.updateById(product);
        
        System.out.println("商品 " + product.getProductName() + " 预警点计算完成: " + reorderPoint);
    }
    
    @Override
    public void batchCalculateReorderPoints() {
        List<Product> products = this.lambdaQuery()
                .eq(Product::getIsManualReorder, 0)
                .list();
        
        for (Product product : products) {
            calculateDynamicReorderPoint(product.getId());
        }
        System.out.println("批量计算预警点完成，共处理 " + products.size() + " 个商品");
    }
    
    @Override
    @Transactional
    public void updateDemandStatistics(Integer productId) {
        String sql =
            "UPDATE product_info p " +
            "LEFT JOIN ( " +
            "    SELECT " +
            "        AVG(daily_quantity) AS avg_daily, " +
            "        STDDEV_SAMP(daily_quantity) AS std_dev " +
            "    FROM ( " +
            "        SELECT " +
            "            DATE(create_time) AS out_date, " +
            "            SUM(quantity) AS daily_quantity " +
            "        FROM stock_out_record " +
            "        WHERE product_id = ? " +
            "          AND create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "        GROUP BY DATE(create_time) " +
            "    ) daily " +
            ") stats ON 1=1 " +
            "SET " +
            "    p.avg_daily_demand = COALESCE(stats.avg_daily, 0), " +
            "    p.demand_std_dev = COALESCE(stats.std_dev, 0) " +
            "WHERE p.id = ? AND p.is_manual_reorder = 0";
        int affectedRows = jdbcTemplate.update(sql, productId, productId);
        System.out.println("商品 " + productId + " 需求统计更新完成" +
            (affectedRows > 0 ? "，数据已更新" : "（商品不存在或人工锁定模式）"));
    }
    
    @Override
    @Transactional
    public void batchUpdateDemandStatistics() {
        String sql =
            "UPDATE product_info p " +
            "LEFT JOIN ( " +
            "    SELECT " +
            "        product_id, " +
            "        AVG(daily_quantity) AS avg_daily, " +
            "        STDDEV_SAMP(daily_quantity) AS std_dev " +
            "    FROM ( " +
            "        SELECT " +
            "            product_id, " +
            "            DATE(create_time) AS out_date, " +
            "            SUM(quantity) AS daily_quantity " +
            "        FROM stock_out_record " +
            "        WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
            "        GROUP BY product_id, DATE(create_time) " +
            "    ) daily " +
            "    GROUP BY product_id " +
            ") stats ON p.id = stats.product_id " +
            "SET " +
            "    p.avg_daily_demand = COALESCE(stats.avg_daily, 0), " +
            "    p.demand_std_dev = COALESCE(stats.std_dev, 0) " +
            "WHERE p.is_manual_reorder = 0";
        int affectedRows = jdbcTemplate.update(sql);
        System.out.println("批量需求统计更新完成，共更新 " + affectedRows + " 个商品");
    }
    
    @Override
    public boolean isStockBelowReorderPoint(Integer productId) {
        Product product = this.getById(productId);
        if (product == null || product.getReorderPoint() == null) {
            return false;
        }
        return java.math.BigDecimal.valueOf(product.getStockLevel()).compareTo(product.getReorderPoint()) < 0;
    }
    
    @Override
    @Transactional
    public boolean checkAndTriggerAlert(Integer productId, Integer deductQuantity) {
        Product product = this.getById(productId);
        if (product == null) return false;
        
        java.math.BigDecimal reorderPoint = product.getReorderPoint();
        if (reorderPoint == null) return false;
        
        java.math.BigDecimal afterDeduct = java.math.BigDecimal.valueOf(product.getStockLevel() - deductQuantity);
        
        if (afterDeduct.compareTo(reorderPoint) < 0) {
            if (product.getAlertStatus() == 0) {
                product.setAlertStatus(1);
                this.updateById(product);
                sendAlertNotification(product, afterDeduct.doubleValue(), reorderPoint.doubleValue());
                return true;
            }
        } else {
            if (product.getAlertStatus() == 1) {
                product.setAlertStatus(0);
                this.updateById(product);
                System.out.println("商品 " + product.getProductName() + " 库存已恢复至预警线以上，预警状态已重置。");
            }
        }
        return false;
    }
    

}