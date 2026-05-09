package com.example.seckill.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Phase 18: Prometheus 指标服务
 * 
 * 封装 Micrometer，提供四种核心指标类型：
 * 1. Counter - 计数器（只增不减）
 * 2. Gauge - 仪表盘（瞬时值）
 * 3. Timer - 计时器（耗时统计）
 * 4. DistributionSummary - 分布统计（直方图）
 */
@Service
public class MetricsService {

    private final MeterRegistry registry;

    // 缓存已创建的指标
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    // ==================== Counter 计数器 ====================

    /**
     * 增加计数器
     * 适用场景：请求数、错误数、处理任务数
     */
    public void incrementCounter(String name, String... tags) {
        Counter counter = counters.computeIfAbsent(name + Arrays.toString(tags), k -> Counter.builder(name)
                .tags(tags)
                .description("Counter: " + name)
                .register(registry));
        counter.increment();
    }

    /**
     * 增加指定值
     */
    public void incrementCounter(String name, double amount, String... tags) {
        Counter counter = counters.computeIfAbsent(name + Arrays.toString(tags), k -> Counter.builder(name)
                .tags(tags)
                .register(registry));
        counter.increment(amount);
    }

    // ==================== Gauge 仪表盘 ====================

    /**
     * 注册 Gauge
     * 适用场景：当前连接数、队列大小、内存使用
     */
    public void registerGauge(String name, Supplier<Number> valueSupplier,
            String... tags) {
        Gauge.builder(name, valueSupplier)
                .tags(tags)
                .description("Gauge: " + name)
                .register(registry);
    }

    /**
     * 注册对象的某个数值属性
     */
    public <T> void registerGauge(String name, T obj,
            java.util.function.ToDoubleFunction<T> valueFunction,
            String... tags) {
        Gauge.builder(name, obj, valueFunction)
                .tags(tags)
                .register(registry);
    }

    // ==================== Timer 计时器 ====================

    /**
     * 记录耗时
     * 适用场景：响应时间、处理耗时
     */
    public void recordTime(String name, long durationMs, String... tags) {
        Timer timer = timers.computeIfAbsent(name + Arrays.toString(tags), k -> Timer.builder(name)
                .tags(tags)
                .description("Timer: " + name)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry));
        timer.record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行并计时
     */
    public <T> T recordTime(String name, Supplier<T> action, String... tags) {
        Timer timer = timers.computeIfAbsent(name + Arrays.toString(tags), k -> Timer.builder(name)
                .tags(tags)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry));
        return timer.record(action);
    }

    // ==================== Distribution Summary 分布统计 ====================

    /**
     * 记录分布值
     * 适用场景：请求大小、响应大小
     */
    public void recordDistribution(String name, double value, String... tags) {
        DistributionSummary.builder(name)
                .tags(tags)
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry)
                .record(value);
    }

    // ==================== 指标概念说明 ====================

    /**
     * 获取指标类型说明
     */
    public Map<String, Object> getMetricTypesInfo() {
        Map<String, Object> types = new LinkedHashMap<>();

        types.put("Counter", Map.of(
                "description", "计数器，只增不减，重启归零",
                "useCase", "请求数、错误数、处理任务数",
                "example", "http_requests_total{method=\"GET\"} → 12345",
                "promql", "rate(http_requests_total[5m])"));

        types.put("Gauge", Map.of(
                "description", "仪表盘，可增可减，表示瞬时值",
                "useCase", "当前连接数、队列大小、内存使用",
                "example", "active_connections{service=\"api\"} → 42",
                "promql", "active_connections"));

        types.put("Histogram", Map.of(
                "description", "直方图，记录分布，预定义桶",
                "useCase", "响应时间分布、请求大小分布",
                "example", "http_latency_bucket{le=\"0.1\"} → 1000",
                "promql", "histogram_quantile(0.99, rate(http_latency_bucket[5m]))"));

        types.put("Summary", Map.of(
                "description", "摘要，客户端计算分位数",
                "useCase", "精确的分位数统计",
                "example", "http_latency{quantile=\"0.99\"} → 0.250",
                "note", "不支持聚合，推荐使用 Histogram"));

        return types;
    }

    /**
     * 获取 PromQL 常用查询示例
     */
    public Map<String, String> getPromQLExamples() {
        Map<String, String> examples = new LinkedHashMap<>();

        examples.put("QPS", "rate(http_requests_total[5m])");
        examples.put("错误率", "rate(http_requests_total{status=\"500\"}[5m]) / rate(http_requests_total[5m])");
        examples.put("P99延迟", "histogram_quantile(0.99, rate(http_request_duration_bucket[5m]))");
        examples.put("平均延迟", "rate(http_request_duration_sum[5m]) / rate(http_request_duration_count[5m])");
        examples.put("内存使用率", "(node_memory_total - node_memory_available) / node_memory_total * 100");

        return examples;
    }

    /**
     * 获取 RED 方法（服务监控黄金指标）
     */
    public Map<String, String> getREDMethod() {
        Map<String, String> red = new LinkedHashMap<>();
        red.put("Rate", "请求速率 (QPS)");
        red.put("Errors", "错误率");
        red.put("Duration", "响应时间 (P50/P99)");
        return red;
    }
}
