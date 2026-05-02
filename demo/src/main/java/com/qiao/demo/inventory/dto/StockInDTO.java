package com.qiao.demo.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "入库请求")
public class StockInDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID", example = "1")
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    @Schema(description = "入库数量", example = "20")
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer quantity;

    @Schema(description = "消息唯一标识（由系统自动生成，用于幂等性保护）")
    private String messageId;

    public StockInDTO() {}

    public StockInDTO(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
}
