package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 实时数据分析平台 - 启动类
 * 
 * Phase 22 实战项目：基于 Spring Boot + Spark 的实时数据分析平台
 * 
 * 本项目展示如何将 Apache Spark 集成到 Spring Boot 企业级应用中，
 * 实现实时数据分析与批量数据处理功能。
 * 
 * 核心功能：
 * 1. 数据采集 - 接收用户行为数据
 * 2. 实时分析 - Spark Streaming 实时计算
 * 3. 批量分析 - Spark SQL 离线分析
 * 4. 指标服务 - REST API 数据查询
 * 
 * 注意：当前使用 Mock 模式运行，禁用 Redis 以简化启动
 * 
 * @author Java Course
 * 
 */
@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
public class RealtimeAnalyticsApplication {

    public static void main(String[] args) {
        // 设置 Spark 相关系统属性
        System.setProperty("spark.master", "local[*]");

        SpringApplication.run(RealtimeAnalyticsApplication.class, args);
    }
}
