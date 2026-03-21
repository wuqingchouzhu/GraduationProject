package com.qiao.demo.inventory.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
}