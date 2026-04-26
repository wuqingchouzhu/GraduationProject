package com.qiao.demo.inventory.dto;

import java.io.Serializable;

public class StockOutDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;
    private Integer quantity;
    private String customerName;

    public StockOutDTO() {}

    public StockOutDTO(Integer productId, Integer quantity, String customerName) {
        this.productId = productId;
        this.quantity = quantity;
        this.customerName = customerName;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}
