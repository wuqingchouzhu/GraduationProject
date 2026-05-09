package com.qiao.demo.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiao.demo.inventory.common.PasswordUtil;
import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.dto.LoginDTO;
import com.qiao.demo.inventory.model.User;
import com.qiao.demo.inventory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证接口", description = "用户登录认证")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录", description = "使用用户名和密码进行登录，返回用户信息和角色")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        User user = userService.getOne(wrapper);

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        boolean valid = PasswordUtil.verify(password, user.getPassword());

        if (!valid && password.equals(user.getPassword())) {
            user.setPassword(PasswordUtil.hash(password));
            userService.updateById(user);
            valid = true;
        }

        if (valid) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("role", user.getRole());
            return Result.success(data);
        }

        return Result.error("用户名或密码错误");
    }
}