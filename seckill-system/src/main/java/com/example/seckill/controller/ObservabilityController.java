package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.observability.alert.AlertService;
import com.example.seckill.observability.logging.StructuredLogger;
import com.example.seckill.observability.metrics.MetricsService;
import com.example.seckill.observability.metrics.SeckillMetrics;
import com.example.seckill.observability.tracing.TracingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 18: 可观测性演示控制器
 * 
 * 提供链路追踪、指标监控、结构化日志、告警规则的演示 API
 */
@RestController
@RequestMapping("/api/demo/observability")
@Tag(name = "可观测性演示", description = "Phase 18 - 链路追踪、指标、日志、告警")
public class ObservabilityController {

    private final TracingService tracingService;
    private final MetricsService metricsService;
    private final SeckillMetrics seckillMetrics;
    private final StructuredLogger structuredLogger;
    private final AlertService alertService;

    public ObservabilityController(TracingService tracingService,
            MetricsService metricsService,
            SeckillMetrics seckillMetrics,
            StructuredLogger structuredLogger,
            AlertService alertService) {
        this.tracingService = tracingService;
        this.metricsService = metricsService;
        this.seckillMetrics = seckillMetrics;
        this.structuredLogger = structuredLogger;
        this.alertService = alertService;
    }

    // ==================== 链路追踪 ====================

    @GetMapping("/trace/current")
    @Operation(summary = "获取当前请求 TraceId", description = "展示当前请求的链路追踪上下文")
    public Result<Map<String, Object>> getCurrentTrace() {
        return Result.success("当前链路信息", tracingService.getCurrentTraceInfo());
    }

    @PostMapping("/trace/span")
    @Operation(summary = "创建自定义 Span", description = "演示手动创建 Span 进行业务埋点")
    public Result<Map<String, Object>> createSpan(
            @RequestParam String operationName,
            @RequestParam(defaultValue = "100") long durationMs) {

        TracingService.SpanInfo span = tracingService.startSpan(operationName);
        tracingService.addTag(span, "demo", "true");

        // 模拟业务处理
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            tracingService.recordError(span, e);
        }

        tracingService.endSpan(span);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("traceId", span.traceId);
        result.put("spanId", span.spanId);
        result.put("operationName", span.operationName);
        result.put("durationMs", span.duration);

        return Result.success("Span 已创建", result);
    }

    @GetMapping("/trace/concepts")
    @Operation(summary = "链路追踪概念说明")
    public Result<Map<String, Object>> getTracingConcepts() {
        return Result.success("链路追踪概念", tracingService.getTracingConcepts());
    }

    @GetMapping("/trace/recent")
    @Operation(summary = "获取最近的 Span 记录")
    public Result<?> getRecentSpans(@RequestParam(defaultValue = "10") int limit) {
        return Result.success("最近 Span", tracingService.getRecentSpans(limit));
    }

    // ==================== 指标监控 ====================

    @GetMapping("/metrics/seckill")
    @Operation(summary = "获取秒杀业务指标")
    public Result<Map<String, Object>> getSeckillMetrics() {
        return Result.success("秒杀业务指标", seckillMetrics.getStats());
    }

    @PostMapping("/metrics/record")
    @Operation(summary = "记录自定义指标", description = "演示 Counter、Timer 指标记录")
    public Result<String> recordMetrics(
            @RequestParam(defaultValue = "custom_counter") String counterName,
            @RequestParam(defaultValue = "custom_timer") String timerName,
            @RequestParam(defaultValue = "100") long durationMs) {

        metricsService.incrementCounter(counterName, "source", "demo");
        metricsService.recordTime(timerName, durationMs, "source", "demo");

        return Result.success("指标已记录：" + counterName + ", " + timerName, null);
    }

    @GetMapping("/metrics/types")
    @Operation(summary = "指标类型说明", description = "Counter、Gauge、Histogram、Summary 四种类型")
    public Result<Map<String, Object>> getMetricTypes() {
        return Result.success("Prometheus 指标类型", metricsService.getMetricTypesInfo());
    }

    @GetMapping("/metrics/promql")
    @Operation(summary = "PromQL 查询示例")
    public Result<Map<String, String>> getPromQLExamples() {
        return Result.success("PromQL 常用查询", metricsService.getPromQLExamples());
    }

    @GetMapping("/metrics/red")
    @Operation(summary = "RED 方法 - 服务监控黄金指标")
    public Result<Map<String, String>> getREDMethod() {
        return Result.success("RED 方法", metricsService.getREDMethod());
    }

    // ==================== 结构化日志 ====================

    @PostMapping("/log/structured")
    @Operation(summary = "结构化日志演示", description = "记录 JSON 格式的结构化日志")
    public Result<String> logStructured(
            @RequestParam String event,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String message) {

        Map<String, Object> fields = new LinkedHashMap<>();
        if (message != null) {
            fields.put("message", message);
        }
        fields.put("demo", true);

        structuredLogger.logEvent(event, userId, fields);

        return Result.success("日志已记录，请查看控制台输出", null);
    }

    @GetMapping("/log/levels")
    @Operation(summary = "日志级别使用规范")
    public Result<Map<String, Object>> getLogLevels() {
        return Result.success("日志级别规范", structuredLogger.getLogLevelGuide());
    }

    @GetMapping("/log/best-practices")
    @Operation(summary = "日志最佳实践")
    public Result<Map<String, Object>> getLogBestPractices() {
        return Result.success("日志最佳实践", structuredLogger.getBestPractices());
    }

    // ==================== 告警规则 ====================

    @GetMapping("/alerts")
    @Operation(summary = "获取告警规则列表")
    public Result<?> getAlertRules() {
        return Result.success("告警规则", alertService.getRules());
    }

    @PostMapping("/alerts/check")
    @Operation(summary = "执行告警检测", description = "检查当前指标是否触发告警")
    public Result<?> checkAlerts() {
        var triggered = alertService.checkAlerts();
        if (triggered.isEmpty()) {
            return Result.success("无告警触发", triggered);
        }
        return Result.success("触发告警: " + triggered.size() + " 条", triggered);
    }

    @GetMapping("/alerts/history")
    @Operation(summary = "获取告警历史")
    public Result<?> getAlertHistory(@RequestParam(defaultValue = "20") int limit) {
        return Result.success("告警历史", alertService.getAlertHistory(limit));
    }

    @GetMapping("/alerts/levels")
    @Operation(summary = "告警级别说明")
    public Result<?> getAlertLevels() {
        return Result.success("告警级别", alertService.getSeverityLevels());
    }

    @GetMapping("/alerts/types")
    @Operation(summary = "告警类型说明")
    public Result<?> getAlertTypes() {
        return Result.success("告警类型", alertService.getAlertTypes());
    }

    @GetMapping("/alerts/best-practices")
    @Operation(summary = "告警最佳实践")
    public Result<Map<String, Object>> getAlertBestPractices() {
        return Result.success("告警最佳实践", alertService.getBestPractices());
    }

    // ==================== 综合概览 ====================

    @GetMapping("/overview")
    @Operation(summary = "可观测性三大支柱概览")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        overview.put("pillars", Map.of(
                "Traces", "追踪请求在多服务的流转，定位分布式调用问题",
                "Metrics", "聚合的数值指标，监控趋势和告警",
                "Logs", "离散的事件记录，详细问题排查"));

        overview.put("currentTrace", tracingService.getCurrentTraceInfo());
        overview.put("seckillMetrics", seckillMetrics.getStats());

        return Result.success("可观测性概览", overview);
    }
}
