package com.qiao.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qiao.demo.inventory.dao")
public class DemoApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        System.out.println("====================================================");
        System.out.println("🚀 进销存系统后端 API 服务启动中...");
        System.out.println("🔗 接口测试地址: http://localhost:8080/api/product/list");
        System.out.println("====================================================");
        SpringApplication.run(DemoApplication.class, args);
        
        System.out.println("====================================================");
        System.out.println("🚀 进销存系统后端 API 服务启动成功！");
        System.out.println("🔗 接口测试地址: http://localhost:8080/api/product/list");
        System.out.println("====================================================");
    }
}