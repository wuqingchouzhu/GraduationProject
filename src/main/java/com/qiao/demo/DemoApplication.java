package com.qiao.demo;

import com.qiao.demo.inventory.view.LoginWindow;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
@MapperScan("com.qiao.demo.inventory.dao")
public class DemoApplication {

    public static void main(String[] args) {
        // 设置 Swing 窗口风格为 Windows 原生风格
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 1. 启动 Spring Boot，加载所有配置和数据库连接
        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 2. 启动前端界面：从容器中取出 LoginWindow 并显示
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = context.getBean(LoginWindow.class);
            loginWindow.setVisible(true); 
        });
    }
}