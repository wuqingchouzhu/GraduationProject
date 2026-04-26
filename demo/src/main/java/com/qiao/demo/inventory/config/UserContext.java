package com.qiao.demo.inventory.config;

import com.qiao.demo.inventory.model.User;

/**
 * 全局用户上下文：存储当前登录的用户信息
 */
public class UserContext {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    // 判断是否为管理员
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}