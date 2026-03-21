package com.qiao.demo.inventory.dao;

import com.qiao.demo.inventory.model.Product;
import java.util.List;

public interface ProductDao {
    // 增加商品
    int insert(Product product);
    
    // 根据 ID 删除商品
    int deleteById(Integer id);
    
    // 修改商品信息
    int update(Product product);
    
    // 查询所有商品
    List<Product> selectAll();
}