package com.qiao.demo;

import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("测试：数据库连接与全表查询")
    void testSelectAll() {
        System.out.println("---- 开始执行全表扫描测试 ----");
        
        List<Product> list = productService.list();
        
        assertNotNull(list, "查询结果集不应为 null");
        
        if (list.isEmpty()) {
            System.out.println("警告：当前数据库表 product_info 为空，请先在 MySQL 中插入数据。");
        } else {
            System.out.println("成功获取数据，记录条数：" + list.size());
            list.forEach(System.out::println);
        }
    }

    @Test
    @DisplayName("测试：新增商品功能")
    void testInsertProduct() {
        System.out.println("---- 开始执行新增商品测试 ----");
        
        Product product = new Product();
        product.setProductCode("TEST-002");
        product.setProductName("测试商品-导师演示");
        product.setStockLevel(100);
        product.setUnitPrice(new BigDecimal("99.99"));

        boolean success = productService.save(product);
        
        assertTrue(success, "商品插入数据库应当返回成功");
        System.out.println("插入成功，生成的 ID 为：" + product.getId());
    }
}