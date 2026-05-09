package com.example.loganalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 日志分析系统启动类
 * 
 * Phase 21 实战项目：基于 Spring Boot 的日志分析系统
 * 模拟 MapReduce 处理 Web 服务器日志
 * 
 * 功能：
 * - PV/UV 统计
 * - 热门页面 Top N
 * - 用户访问路径分析
 * 
 * @author Java Course
 * @since Phase 21
 */
@SpringBootApplication
public class LogAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogAnalyzerApplication.class, args);
        System.out.println("\n=================================");
        System.out.println("  日志分析系统启动成功！");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("=================================\n");
    }
}
