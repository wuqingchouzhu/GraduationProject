package com.qiao.demo.inventory.controller;

import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.dto.StockInDTO;
import com.qiao.demo.inventory.dto.StockOutDTO;
import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.mq.StockInProducer;
import com.qiao.demo.inventory.mq.StockOutProducer;
import com.qiao.demo.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "商品管理", description = "商品的增删改查、出入库操作及库存预警管理")
@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockOutProducer stockOutProducer;

    @Autowired
    private StockInProducer stockInProducer;

    @Operation(summary = "获取商品列表", description = "查询所有商品信息，包括库存状态和预警状态")
    @GetMapping("/list")
    public Result<List<Product>> getProductList() {
        return Result.success(productService.list());
    }

    @Operation(summary = "商品入库", description = "通过RabbitMQ异步处理入库请求")
    @PostMapping("/stockIn")
    public Result<?> stockIn(@Valid @RequestBody StockInDTO dto) {
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            return Result.error("入库失败：商品不存在，ID=" + dto.getProductId());
        }
        stockInProducer.send(dto);
        return Result.success("请求已提交，处理中");
    }

    @Operation(summary = "商品出库", description = "通过RabbitMQ异步处理出库请求")
    @PostMapping("/stockOut")
    public Result<?> stockOut(@Valid @RequestBody StockOutDTO dto) {
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            return Result.error("出库失败：商品不存在，ID=" + dto.getProductId());
        }
        if (product.getStockLevel() < dto.getQuantity()) {
            return Result.error("出库失败：库存不足！当前仅剩 " + product.getStockLevel() + " 件。");
        }

        if (dto.getCustomerName() == null) {
            dto.setCustomerName("默认出库客户");
        }
        stockOutProducer.send(dto);
        return Result.success("请求已提交，处理中");
    }

    @Operation(summary = "新增商品", description = "添加新商品到库存系统中")
    @PostMapping("/add")
    public Result<?> addProduct(@Valid @RequestBody Product product) {
        product.setStockLevel(0);
        product.setVersion(1);
        productService.save(product);
        return Result.success();
    }

    @Operation(summary = "删除商品", description = "根据商品ID删除商品")
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteProduct(@Parameter(description = "商品ID") @PathVariable Integer id) {
        boolean removed = productService.removeById(id);
        if (removed) {
            return Result.success();
        } else {
            return Result.error("删除失败，商品可能已被移除");
        }
    }

    @Operation(summary = "手动触发预警重算", description = "统计近30天需求数据并重新计算各商品的动态预警点")
    @PostMapping("/recalculate")
    public Result<?> recalculateAlerts() {
        try {
            productService.batchUpdateDemandStatistics();
            productService.batchCalculateReorderPoints();
            return Result.success();
        } catch (Exception e) {
            return Result.error("预警重算失败：" + e.getMessage());
        }
    }

    @Operation(summary = "修改商品信息", description = "管理员修改已有商品的编号、名称、单价和补货天数")
    @PutMapping("/update/{id}")
    public Result<?> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        Object productCode = body.get("productCode");
        if (productCode != null) {
            product.setProductCode(productCode.toString());
        }
        Object productName = body.get("productName");
        if (productName != null) {
            product.setProductName(productName.toString());
        }
        Object unitPriceObj = body.get("unitPrice");
        if (unitPriceObj != null) {
            BigDecimal unitPrice;
            if (unitPriceObj instanceof BigDecimal) {
                unitPrice = (BigDecimal) unitPriceObj;
            } else {
                unitPrice = new BigDecimal(unitPriceObj.toString());
            }
            product.setUnitPrice(unitPrice);
        }
        Object leadTimeObj = body.get("leadTime");
        if (leadTimeObj != null) {
            product.setLeadTime(leadTimeObj instanceof Integer
                    ? (Integer) leadTimeObj : Integer.valueOf(leadTimeObj.toString()));
        }
        productService.updateById(product);
        return Result.success();
    }
}