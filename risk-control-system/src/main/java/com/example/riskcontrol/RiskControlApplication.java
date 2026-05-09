package com.example.riskcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 实时风控系统 - 启动类
 * 
 * 基于 Flink CEP 概念的实时风控系统
 * 
 * 本项目使用 Mock 模式模拟 Flink 核心功能：
 * - CEP: 复杂事件处理（模式匹配）
 * - State: 状态管理（用户交易历史）
 * - Window: 窗口聚合（时间窗口统计）
 * - Broadcast State: 动态规则广播
 * 
 * 核心功能：
 * 1. 实时交易风险检测
 * 2. 复杂事件模式识别（连续登录失败、异地交易、大额交易）
 * 3. 动态规则引擎
 * 4. 风险评分计算
 * 
 * @author Java Course
 * @since Phase 23
 */
@SpringBootApplication
@EnableScheduling
public class RiskControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskControlApplication.class, args);
    }
}
