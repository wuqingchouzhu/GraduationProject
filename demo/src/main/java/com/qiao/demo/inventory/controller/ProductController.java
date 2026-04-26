package com.qiao.demo.inventory.controller;

import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.dto.StockOutDTO;
import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.mq.StockOutProducer;
import com.qiao.demo.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private StockOutProducer stockOutProducer;

    @GetMapping("/list")
    public Result<List<Product>> getProductList() {
        return Result.success(productService.list());
    }

    @PostMapping("/stockIn")
    public Result<?> stockIn(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer quantity = (Integer) params.get("quantity");
        productService.stockIn(id, quantity);
        return Result.success();
    }

    @PostMapping("/stockOut")
    public Result<?> stockOut(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer quantity = (Integer) params.get("quantity");
        String customerName = (String) params.getOrDefault("customerName", "默认出库客户");

        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("出库失败：商品不存在，ID=" + id);
        }
        if (product.getStockLevel() < quantity) {
            return Result.error("出库失败：库存不足！当前仅剩 " + product.getStockLevel() + " 件。");
        }

        StockOutDTO dto = new StockOutDTO(id, quantity, customerName);
        stockOutProducer.send(dto);
        return Result.success("请求已提交，处理中");
    }

    @PostMapping("/add")
    public Result<?> addProduct(@RequestBody Product product) {
        if (product.getProductCode() == null || product.getProductName() == null) {
            return Result.error("商品编号和名称不能为空");
        }
        product.setStockLevel(0);
        product.setVersion(1);
        productService.save(product);
        return Result.success();
    }
    // 5. 删除商品接口
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteProduct(@PathVariable Integer id) {
        boolean removed = productService.removeById(id);
        if (removed) {
            return Result.success();
        } else {
            return Result.error("删除失败，商品可能已被移除");
        }
    }

    // 6. 手动触发预警重算（统计需求 + 计算预警点）
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
}