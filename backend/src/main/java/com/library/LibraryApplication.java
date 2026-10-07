package com.library;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.library.mapper")
@EnableScheduling
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("========================================");
        System.out.println("  在线图书借阅管理系统启动成功！");
        System.out.println("  接口地址：http://localhost:8080/api");
        System.out.println("========================================");
    }
}
