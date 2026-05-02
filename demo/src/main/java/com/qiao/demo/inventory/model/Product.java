package com.qiao.demo.inventory.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Schema(description = "商品信息")
@TableName("product_info")
public class Product {

    @Schema(description = "商品ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "商品编号", example = "P001")
    @NotBlank(message = "商品编号不能为空")
    private String productCode;

    @Schema(description = "商品名称", example = "示例商品")
    @NotBlank(message = "商品名称不能为空")
    private String productName;

    @Schema(description = "当前库存数量")
    private Integer stockLevel;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;

    @Schema(description = "日均需求量")
    @TableField("avg_daily_demand")
    private BigDecimal avgDailyDemand = BigDecimal.ZERO;
    
    @Schema(description = "需求标准差")
    @TableField("demand_std_dev")
    private BigDecimal demandStdDev = BigDecimal.ZERO;
    
    @Schema(description = "补货天数")
    @TableField("lead_time")
    private Integer leadTime = 1;
    
    @Schema(description = "预警触发点")
    @TableField("reorder_point")
    private BigDecimal reorderPoint = BigDecimal.ZERO;
    
    @Schema(description = "预警模式：0-系统自动计算，1-人工锁定")
    @TableField("is_manual_reorder")
    private Integer isManualReorder = 0;
    
    @Schema(description = "预警状态：0-正常，1-预警中")
    @TableField("alert_status")
    private Integer alertStatus = 0;

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