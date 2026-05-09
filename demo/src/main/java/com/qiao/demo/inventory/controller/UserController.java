package com.qiao.demo.inventory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiao.demo.inventory.common.PasswordUtil;
import com.qiao.demo.inventory.common.Result;
import com.qiao.demo.inventory.model.User;
import com.qiao.demo.inventory.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "用户管理", description = "管理员用户CRUD及密码验证")
@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    private boolean verifyAdminPassword(String adminPassword) {
        if (adminPassword == null || adminPassword.isEmpty()) {
            return false;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", "admin").eq("role", "ADMIN");
        User admin = userService.getOne(wrapper);
        if (admin == null) {
            return false;
        }
        if (PasswordUtil.verify(adminPassword, admin.getPassword())) {
            return true;
        }
        if (adminPassword.equals(admin.getPassword())) {
            admin.setPassword(PasswordUtil.hash(adminPassword));
            userService.updateById(admin);
            return true;
        }
        return false;
    }

    @Operation(summary = "查询所有用户", description = "获取所有系统用户列表（不包含密码字段）")
    @GetMapping("/api/admin/users")
    public Result<List<Map<String, Object>>> listAllUsers() {
        List<User> users = userService.list();
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("role", u.getRole());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @Operation(summary = "添加新用户", description = "验证管理员密码后添加新用户，用户名不可重复")
    @PostMapping("/api/admin/users")
    public Result<?> addUser(@RequestBody Map<String, Object> body) {
        String adminPassword = (String) body.get("adminPassword");
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        if (!verifyAdminPassword(adminPassword)) {
            return Result.error("管理员密码验证失败");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        if (userService.count(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPassword(PasswordUtil.hash(password));
        newUser.setRole(role != null ? role : "USER");
        userService.save(newUser);

        Map<String, Object> data = new HashMap<>();
        data.put("id", newUser.getId());
        data.put("username", newUser.getUsername());
        data.put("role", newUser.getRole());
        return Result.success(data);
    }

    @Operation(summary = "删除用户", description = "验证管理员密码后删除指定用户，不能删除管理员自己")
    @DeleteMapping("/api/admin/users/{id}")
    public Result<?> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        String adminPassword = (String) body.get("adminPassword");

        if (!verifyAdminPassword(adminPassword)) {
            return Result.error("管理员密码验证失败");
        }

        User targetUser = userService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }

        if ("admin".equals(targetUser.getUsername())) {
            return Result.error("不能删除自己");
        }

        userService.removeById(id);
        return Result.success("用户删除成功");
    }

    @Operation(summary = "验证管理员密码", description = "独立的管理员密码验证接口")
    @PostMapping("/api/auth/verify-password")
    public Result<?> verifyPassword(@RequestBody Map<String, Object> body) {
        String password = (String) body.get("password");

        if (verifyAdminPassword(password)) {
            return Result.success("验证成功");
        }
        return Result.error("密码错误");
    }
}
