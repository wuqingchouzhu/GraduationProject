package com.qiao.demo.inventory.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "库存出入库记录")
public class StockRecord {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "客户名称（出库时有值，入库时为 N/A）")
    private String customerName;

    @Schema(description = "记录类型：STOCK_IN 或 STOCK_OUT")
    private String type;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "消息ID（用于幂等性校验）")
    private String messageId;

    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}
