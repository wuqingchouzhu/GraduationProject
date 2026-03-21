package com.qiao.demo.inventory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiao.demo.inventory.model.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring 这是一个数据库映射组件
public interface ProductMapper extends BaseMapper<Product> {
    // 基础的 insert, delete, update, selectById 已经由 BaseMapper 自动提供了
    // 除非有极其复杂的自定义 SQL，否则不需要写实现类
}