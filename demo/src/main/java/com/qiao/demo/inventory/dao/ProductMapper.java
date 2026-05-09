package com.qiao.demo.inventory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiao.demo.inventory.model.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}