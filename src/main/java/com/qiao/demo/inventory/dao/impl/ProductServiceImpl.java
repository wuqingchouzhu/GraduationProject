package com.qiao.demo.inventory.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiao.demo.inventory.dao.ProductMapper;
import com.qiao.demo.inventory.model.Product;
import com.qiao.demo.inventory.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private JdbcTemplate jdbcTemplate; // 用于执行流水表的插入 SQL

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启数据库事务，保证原子性
    public boolean stockIn(Integer productId, int quantity) {
        Product product = this.getById(productId);
        if (product == null) return false;

        // 1. 更新库存 (乐观锁会自动校验 version)
        product.setStockLevel(product.getStockLevel() + quantity);
        boolean updateSuccess = this.updateById(product);
        
        if (!updateSuccess) {
            // 抛出异常会触发事务回滚，终止所有操作
            throw new RuntimeException("并发冲突：商品数据已被修改，请刷新后重试！"); 
        }

        // 2. 写入入库流水记录
        String sql = "INSERT INTO stock_in_record (product_id, quantity, operator) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, productId, quantity, "系统管理员");

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启数据库事务
    public boolean stockOut(Integer productId, int quantity) {
        Product product = this.getById(productId);
        if (product == null) return false;

        if (product.getStockLevel() < quantity) {
            throw new RuntimeException("出库失败：库存不足！当前仅剩 " + product.getStockLevel() + " 件。");
        }

        // 1. 更新库存
        product.setStockLevel(product.getStockLevel() - quantity);
        boolean updateSuccess = this.updateById(product);
        
        if (!updateSuccess) {
            throw new RuntimeException("并发冲突：商品数据已被修改，请刷新后重试！"); 
        }

        // 2. 写入出库流水记录
        String sql = "INSERT INTO stock_out_record (product_id, quantity, customer_name) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, productId, quantity, "默认出库客户");

        return true;
    }
}