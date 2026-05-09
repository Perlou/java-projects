package com.example.seckill.observability.alert;

import com.example.seckill.observability.metrics.SeckillMetrics;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Phase 18: 告警服务
 * 
 * 管理告警规则，执行告警检测，记录告警历史
 */
@Service
public class AlertService {

    private final SeckillMetrics seckillMetrics;

    // 预定义告警规则
    private final List<AlertRule> rules = new ArrayList<>();

    // 告警历史
    private final List<AlertEvent> alertHistory = Collections.synchronizedList(new ArrayList<>());

    public AlertService(SeckillMetrics seckillMetrics) {
        this.seckillMetrics = seckillMetrics;
        initDefaultRules();
    }

    /**
     * 初始化默认告警规则
     */
    private void initDefaultRules() {
        // 高错误率告警
        rules.add(AlertRule.builder()
                .name("HighErrorRate")
                .type(AlertRule.AlertType.THRESHOLD)
                .severity(AlertRule.Severity.P0_CRITICAL)
                .metric("seckill_fail_rate")
                .operator(AlertRule.Operator.GREATER_THAN)
                .threshold(0.05)
                .durationMinutes(5)
                .description("秒杀失败率超过 5%")
                .enabled(true)
                .build());

        // 高延迟告警
        rules.add(AlertRule.builder()
                .name("HighLatency")
                .type(AlertRule.AlertType.THRESHOLD)
                .severity(AlertRule.Severity.P1_HIGH)
                .metric("seckill_latency_p99")
                .operator(AlertRule.Operator.GREATER_THAN)
                .threshold(1000)
                .durationMinutes(10)
                .description("秒杀接口 P99 延迟超过 1 秒")
                .enabled(true)
                .build());

        // 队列积压告警
        rules.add(AlertRule.builder()
                .name("QueueBacklog")
                .type(AlertRule.AlertType.THRESHOLD)
                .severity(AlertRule.Severity.P2_MEDIUM)
                .metric("seckill_queue_size")
                .operator(AlertRule.Operator.GREATER_THAN)
                .threshold(1000)
                .durationMinutes(5)
                .description("消息队列积压超过 1000")
                .enabled(true)
                .build());

        // 服务不可用告警
        rules.add(AlertRule.builder()
                .name("ServiceDown")
                .type(AlertRule.AlertType.HEARTBEAT)
                .severity(AlertRule.Severity.P0_CRITICAL)
                .metric("up")
                .operator(AlertRule.Operator.EQUAL)
                .threshold(0)
                .durationMinutes(1)
                .description("服务不可用")
                .enabled(true)
                .build());
    }

    /**
     * 获取所有告警规则
     */
    public List<AlertRule> getRules() {
        return new ArrayList<>(rules);
    }

    /**
     * 添加告警规则
     */
    public void addRule(AlertRule rule) {
        rules.add(rule);
    }

    /**
     * 执行告警检测（演示）
     */
    public List<AlertEvent> checkAlerts() {
        List<AlertEvent> triggered = new ArrayList<>();
        Map<String, Object> stats = seckillMetrics.getStats();

        for (AlertRule rule : rules) {
            if (!rule.isEnabled())
                continue;

            double currentValue = getCurrentMetricValue(rule.getMetric(), stats);
            boolean shouldAlert = evaluateRule(rule, currentValue);

            if (shouldAlert) {
                AlertEvent event = new AlertEvent();
                event.ruleName = rule.getName();
                event.severity = rule.getSeverity();
                event.metric = rule.getMetric();
                event.currentValue = currentValue;
                event.threshold = rule.getThreshold();
                event.timestamp = Instant.now().toString();
                event.message = rule.getDescription();

                triggered.add(event);
                alertHistory.add(event);
            }
        }

        // 保持最近 100 条
        while (alertHistory.size() > 100) {
            alertHistory.remove(0);
        }

        return triggered;
    }

    /**
     * 获取告警历史
     */
    public List<AlertEvent> getAlertHistory(int limit) {
        int size = alertHistory.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(alertHistory.subList(start, size));
    }

    /**
     * 获取告警级别说明
     */
    public List<Map<String, Object>> getSeverityLevels() {
        List<Map<String, Object>> levels = new ArrayList<>();
        for (AlertRule.Severity severity : AlertRule.Severity.values()) {
            levels.add(Map.of(
                    "level", severity.name(),
                    "displayName", severity.getDisplayName(),
                    "responseTime", severity.getResponseTime(),
                    "description", severity.getDescription()));
        }
        return levels;
    }

    /**
     * 获取告警类型说明
     */
    public List<Map<String, Object>> getAlertTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (AlertRule.AlertType type : AlertRule.AlertType.values()) {
            types.add(Map.of(
                    "type", type.name(),
                    "displayName", type.getDisplayName(),
                    "description", type.getDescription()));
        }
        return types;
    }

    /**
     * 获取告警最佳实践
     */
    public Map<String, Object> getBestPractices() {
        Map<String, Object> practices = new LinkedHashMap<>();

        practices.put("principles", List.of(
                "精准性 - 减少误报，告警必须反映真实问题",
                "及时性 - 检测延迟 < 5 分钟，通知延迟 < 1 分钟",
                "可操作性 - 告警包含上下文，关联 Runbook",
                "分级明确 - 区分 Critical / Warning / Info"));

        practices.put("antiPatterns", List.of(
                "告警太多，开始忽略（告警疲劳）",
                "误报频繁，失去信任",
                "告警信息不清楚，无法行动"));

        practices.put("solutions", Map.of(
                "Inhibition", "更严重告警触发时，抑制次要告警",
                "Grouping", "相同类型告警合并为一条通知",
                "Silence", "维护期间临时静默",
                "Review", "定期审查，删除无效告警"));

        return practices;
    }

    // ==================== 内部方法 ====================

    private double getCurrentMetricValue(String metric, Map<String, Object> stats) {
        return switch (metric) {
            case "seckill_fail_rate" -> {
                double total = ((Number) stats.getOrDefault("requestsTotal", 1.0)).doubleValue();
                double fail = ((Number) stats.getOrDefault("failTotal", 0.0)).doubleValue();
                yield total > 0 ? fail / total : 0;
            }
            case "seckill_queue_size" ->
                ((Number) stats.getOrDefault("currentQueueSize", 0)).doubleValue();
            default -> 0;
        };
    }

    private boolean evaluateRule(AlertRule rule, double currentValue) {
        return switch (rule.getOperator()) {
            case GREATER_THAN -> currentValue > rule.getThreshold();
            case LESS_THAN -> currentValue < rule.getThreshold();
            case GREATER_EQUAL -> currentValue >= rule.getThreshold();
            case LESS_EQUAL -> currentValue <= rule.getThreshold();
            case EQUAL -> currentValue == rule.getThreshold();
            case NOT_EQUAL -> currentValue != rule.getThreshold();
        };
    }

    // ==================== 告警事件类 ====================

    public static class AlertEvent {
        public String ruleName;
        public AlertRule.Severity severity;
        public String metric;
        public double currentValue;
        public double threshold;
        public String timestamp;
        public String message;
    }
}
