package com.qiao.demo.inventory.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;

@TableName("product_info")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String productCode;
    private String productName;
    private Integer stockLevel;
    private BigDecimal unitPrice;

    // 乐观锁版本号，MyBatis-Plus 会自动维护
    @Version
    private Integer version;

    // 动态库存预警字段
    @TableField("avg_daily_demand")
    private BigDecimal avgDailyDemand = BigDecimal.ZERO;          // 日均需求量
    
    @TableField("demand_std_dev")
    private BigDecimal demandStdDev = BigDecimal.ZERO;            // 需求标准差
    
    @TableField("lead_time")
    private Integer leadTime = 1;                   // 补货天数
    
    @TableField("reorder_point")
    private BigDecimal reorderPoint = BigDecimal.ZERO;            // 预警触发点
    
    @TableField("is_manual_reorder")
    private Integer isManualReorder = 0;            // 预警模式标志：0-系统自动计算，1-人工锁定
    
    @TableField("alert_status")
    private Integer alertStatus = 0;                // 预警状态：0-正常，1-预警中

    // --- Getter & Setter ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getStockLevel() { return stockLevel; }
    public void setStockLevel(Integer stockLevel) { this.stockLevel = stockLevel; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public BigDecimal getAvgDailyDemand() { return avgDailyDemand; }
    public void setAvgDailyDemand(BigDecimal avgDailyDemand) { this.avgDailyDemand = avgDailyDemand; }

    public BigDecimal getDemandStdDev() { return demandStdDev; }
    public void setDemandStdDev(BigDecimal demandStdDev) { this.demandStdDev = demandStdDev; }

    public Integer getLeadTime() { return leadTime; }
    public void setLeadTime(Integer leadTime) { this.leadTime = leadTime; }

    public BigDecimal getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(BigDecimal reorderPoint) { this.reorderPoint = reorderPoint; }

    public Integer getIsManualReorder() { return isManualReorder; }
    public void setIsManualReorder(Integer isManualReorder) { this.isManualReorder = isManualReorder; }

    public Integer getAlertStatus() { return alertStatus; }
    public void setAlertStatus(Integer alertStatus) { this.alertStatus = alertStatus; }
}