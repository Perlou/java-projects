package com.example.seckill.observability.logging;

import com.example.seckill.observability.tracing.TraceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 18: 结构化日志工具
 * 
 * 生成 JSON 格式的结构化日志，便于 ELK 等系统采集分析
 */
@Component
public class StructuredLogger {

    private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 记录 INFO 级别结构化日志
     */
    public void info(String event, Map<String, Object> fields) {
        log(Level.INFO, event, fields, null);
    }

    /**
     * 记录 WARN 级别结构化日志
     */
    public void warn(String event, Map<String, Object> fields) {
        log(Level.WARN, event, fields, null);
    }

    /**
     * 记录 ERROR 级别结构化日志
     */
    public void error(String event, Map<String, Object> fields, Throwable error) {
        log(Level.ERROR, event, fields, error);
    }

    /**
     * 记录业务事件
     */
    public void logEvent(String event, Long userId, Map<String, Object> data) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("userId", userId);
        if (data != null) {
            fields.putAll(data);
        }
        info(event, fields);
    }

    /**
     * 记录秒杀事件
     */
    public void logSeckill(Long userId, Long goodsId, String action, boolean success,
            Long durationMs, String message) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("userId", userId);
        fields.put("goodsId", goodsId);
        fields.put("action", action);
        fields.put("success", success);
        fields.put("durationMs", durationMs);
        if (message != null) {
            fields.put("message", message);
        }

        if (success) {
            info("seckill." + action, fields);
        } else {
            warn("seckill." + action + ".failed", fields);
        }
    }

    // ==================== 内部方法 ====================

    private void log(Level level, String event, Map<String, Object> fields, Throwable error) {
        Map<String, Object> logEntry = new LinkedHashMap<>();

        // 时间戳
        logEntry.put("@timestamp", Instant.now().toString());

        // 日志级别
        logEntry.put("level", level.name());

        // 服务信息
        logEntry.put("service", "seckill-system");

        // 链路追踪信息
        TraceContext ctx = TraceContext.current();
        if (ctx != null) {
            logEntry.put("traceId", ctx.getTraceId());
            logEntry.put("spanId", ctx.getSpanId());
        }

        // 事件名称
        logEntry.put("event", event);

        // 业务字段
        if (fields != null && !fields.isEmpty()) {
            logEntry.putAll(fields);
        }

        // 错误信息
        if (error != null) {
            logEntry.put("error.type", error.getClass().getName());
            logEntry.put("error.message", error.getMessage());
        }

        // 输出 JSON 日志
        try {
            String json = objectMapper.writeValueAsString(logEntry);
            switch (level) {
                case ERROR -> log.error(json);
                case WARN -> log.warn(json);
                case INFO -> log.info(json);
                case DEBUG -> log.debug(json);
            }
        } catch (Exception e) {
            log.error("Failed to serialize log entry", e);
        }
    }

    /**
     * 获取日志级别使用规范
     */
    public Map<String, Object> getLogLevelGuide() {
        Map<String, Object> guide = new LinkedHashMap<>();

        guide.put("ERROR", Map.of(
                "description", "系统错误，需要立即处理",
                "example", "数据库连接失败、外部服务不可用"));

        guide.put("WARN", Map.of(
                "description", "警告信息，可能导致问题",
                "example", "重试成功、配置使用默认值"));

        guide.put("INFO", Map.of(
                "description", "重要业务事件",
                "example", "订单创建、用户登录、支付完成"));

        guide.put("DEBUG", Map.of(
                "description", "调试信息，开发环境使用",
                "example", "方法入参、中间结果"));

        return guide;
    }

    /**
     * 获取结构化日志最佳实践
     */
    public Map<String, Object> getBestPractices() {
        Map<String, Object> practices = new LinkedHashMap<>();

        practices.put("format", "使用 JSON 格式，机器可解析");
        practices.put("traceId", "所有日志包含 traceId，便于问题追踪");
        practices.put("fields", "包含关键业务字段（userId, orderId 等）");
        practices.put("sensitive", "避免记录敏感信息（密码、身份证号等）");
        practices.put("elk", Map.of(
                "Filebeat", "轻量级日志采集，运行在每个节点",
                "Logstash", "日志解析、过滤、转换",
                "Elasticsearch", "分布式搜索引擎，存储和索引日志",
                "Kibana", "可视化界面，查询和分析日志"));

        return practices;
    }

    public enum Level {
        ERROR, WARN, INFO, DEBUG
    }
}
