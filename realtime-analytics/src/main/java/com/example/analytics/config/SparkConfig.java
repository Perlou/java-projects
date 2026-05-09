package com.example.analytics.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spark 配置类 - 兼容模式
 * 
 * 由于 Spark 3.5 与 Java 18+ 存在兼容性问题，
 * 此配置提供一个模拟的 SparkSession，用于演示 Spark 概念。
 * 
 * 【Java 版本兼容性问题】
 * - Spark 3.5 依赖 Hadoop，而 Hadoop 使用了 Subject.getSubject()
 * - 该方法在 Java 18+ 中已被移除
 * - 若需真实运行 Spark，请使用 Java 11 或 Java 17
 * 
 * @author Java Course
 * 
 */
@Configuration
public class SparkConfig {

    private static final Logger log = LoggerFactory.getLogger(SparkConfig.class);

    @Value("${spark.app-name:realtime-analytics}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String master;

    @Value("${spark.mock-mode:true}")
    private boolean mockMode;

    /**
     * 创建 SparkSessionWrapper Bean
     * 
     * 在 Mock 模式下，提供模拟的 Spark 功能用于学习演示
     */
    @Bean
    @Primary
    public SparkSessionWrapper sparkSessionWrapper() {
        log.info("=== Spark 配置初始化 ===");
        log.info("  App Name: {}", appName);
        log.info("  Master: {}", master);
        log.info("  Mock Mode: {}", mockMode);

        if (mockMode) {
            log.warn("⚠️  运行在 Mock 模式 - 模拟 Spark 操作用于学习演示");
            log.warn("   若需真实 Spark 运行时，请使用 Java 11/17 并设置 spark.mock-mode=false");
            return new SparkSessionWrapper(appName, master, true);
        }

        // 真实模式需要 Java 11/17
        try {
            return createRealSparkSession();
        } catch (Exception e) {
            log.error("创建真实 SparkSession 失败，回退到 Mock 模式: {}", e.getMessage());
            return new SparkSessionWrapper(appName, master, true);
        }
    }

    private SparkSessionWrapper createRealSparkSession() {
        // 尝试创建真实的 SparkSession
        // 这需要 Java 11/17
        throw new UnsupportedOperationException(
                "真实 Spark 模式需要 Java 11 或 Java 17。当前 Java 版本不兼容。");
    }

    /**
     * SparkSession 包装器
     * 
     * 提供统一的接口，支持真实 Spark 和模拟模式
     */
    public static class SparkSessionWrapper {
        private final String appName;
        private final String master;
        private final boolean mockMode;

        public SparkSessionWrapper(String appName, String master, boolean mockMode) {
            this.appName = appName;
            this.master = master;
            this.mockMode = mockMode;
        }

        public String getAppName() {
            return appName;
        }

        public String getMaster() {
            return master;
        }

        public boolean isMockMode() {
            return mockMode;
        }

        public String version() {
            return mockMode ? "3.5.0 (Mock)" : "3.5.0";
        }
    }
}
