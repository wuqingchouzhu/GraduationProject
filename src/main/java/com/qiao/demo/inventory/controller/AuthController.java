package com.qiao.demo.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.model.User;
import com.qiao.demo.inventory.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        // 使用更稳妥的 QueryWrapper 避免 Lambda 编译问题
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("password", password);
        
        User user = userService.getOne(wrapper);

        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            return Result.success(data);
        }

        return Result.error("用户名或密码错误");
    }
}