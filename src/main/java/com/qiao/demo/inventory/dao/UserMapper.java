package com.qiao.demo.inventory.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiao.demo.inventory.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 后，MyBatis-Plus 会自动帮你写好增删改查的 SQL
}