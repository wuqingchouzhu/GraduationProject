package com.qiao.demo.inventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiao.demo.inventory.dao.UserMapper;
import com.qiao.demo.inventory.model.User;
import com.qiao.demo.inventory.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}