package com.example.seckill.observability.alert;

import java.util.Map;

/**
 * Phase 18: 告警规则定义
 * 
 * 定义告警规则的数据结构
 */
public class AlertRule {

    private String name;
    private AlertType type;
    private Severity severity;
    private String metric;
    private Operator operator;
    private double threshold;
    private int durationMinutes;
    private String description;
    private Map<String, String> labels;
    private boolean enabled;

    // ==================== 告警类型 ====================

    public enum AlertType {
        THRESHOLD("阈值告警", "当指标超过/低于阈值时触发"),
        TREND("趋势告警", "基于历史数据预测趋势"),
        ANOMALY("异常检测", "基于机器学习检测异常"),
        HEARTBEAT("心跳告警", "检测服务存活状态");

        private final String displayName;
        private final String description;

        AlertType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 告警级别 ====================

    public enum Severity {
        P0_CRITICAL("P0 紧急", "5分钟", "核心服务完全不可用，影响大量用户"),
        P1_HIGH("P1 严重", "30分钟", "服务降级，功能部分可用"),
        P2_MEDIUM("P2 一般", "4小时", "非核心功能异常，不影响主流程"),
        P3_LOW("P3 提醒", "24小时", "轻微问题，优化建议");

        private final String displayName;
        private final String responseTime;
        private final String description;

        Severity(String displayName, String responseTime, String description) {
            this.displayName = displayName;
            this.responseTime = responseTime;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getResponseTime() {
            return responseTime;
        }

        public String getDescription() {
            return description;
        }
    }

    // ==================== 比较操作符 ====================

    public enum Operator {
        GREATER_THAN(">", "大于"),
        LESS_THAN("<", "小于"),
        GREATER_EQUAL(">=", "大于等于"),
        LESS_EQUAL("<=", "小于等于"),
        EQUAL("==", "等于"),
        NOT_EQUAL("!=", "不等于");

        private final String symbol;
        private final String displayName;

        Operator(String symbol, String displayName) {
            this.symbol = symbol;
            this.displayName = displayName;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ==================== 构建器 ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AlertRule rule = new AlertRule();

        public Builder name(String name) {
            rule.name = name;
            return this;
        }

        public Builder type(AlertType type) {
            rule.type = type;
            return this;
        }

        public Builder severity(Severity severity) {
            rule.severity = severity;
            return this;
        }

        public Builder metric(String metric) {
            rule.metric = metric;
            return this;
        }

        public Builder operator(Operator operator) {
            rule.operator = operator;
            return this;
        }

        public Builder threshold(double threshold) {
            rule.threshold = threshold;
            return this;
        }

        public Builder durationMinutes(int minutes) {
            rule.durationMinutes = minutes;
            return this;
        }

        public Builder description(String description) {
            rule.description = description;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            rule.labels = labels;
            return this;
        }

        public Builder enabled(boolean enabled) {
            rule.enabled = enabled;
            return this;
        }

        public AlertRule build() {
            return rule;
        }
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public AlertType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMetric() {
        return metric;
    }

    public Operator getOperator() {
        return operator;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 生成 Prometheus 告警规则格式
     */
    public String toPrometheusRule() {
        return String.format("""
                - alert: %s
                  expr: %s %s %.2f
                  for: %dm
                  labels:
                    severity: %s
                  annotations:
                    summary: "%s"
                """,
                name, metric, operator.getSymbol(), threshold,
                durationMinutes, severity.name().toLowerCase(), description);
    }
}
