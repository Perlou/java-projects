package com.example.seckill.observability.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Phase 18: 秒杀业务指标
 * 
 * 收集秒杀系统核心业务指标
 */
@Component
public class SeckillMetrics {

    private final MeterRegistry registry;

    // 计数器
    private Counter seckillRequestsTotal;
    private Counter seckillSuccessTotal;
    private Counter seckillFailTotal;
    private Counter seckillSoldOutTotal;

    // 计时器
    private Timer seckillLatency;

    // 仪表盘（瞬时值）
    private final AtomicLong currentQueueSize = new AtomicLong(0);
    private final AtomicLong activeUsers = new AtomicLong(0);

    public SeckillMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        // 秒杀请求总数
        seckillRequestsTotal = Counter.builder("seckill_requests_total")
                .description("秒杀请求总数")
                .tag("application", "seckill-system")
                .register(registry);

        // 秒杀成功数
        seckillSuccessTotal = Counter.builder("seckill_success_total")
                .description("秒杀成功总数")
                .tag("application", "seckill-system")
                .register(registry);

        // 秒杀失败数
        seckillFailTotal = Counter.builder("seckill_fail_total")
                .description("秒杀失败总数")
                .tag("application", "seckill-system")
                .register(registry);

        // 售罄次数
        seckillSoldOutTotal = Counter.builder("seckill_sold_out_total")
                .description("商品售罄次数")
                .tag("application", "seckill-system")
                .register(registry);

        // 秒杀响应时间
        seckillLatency = Timer.builder("seckill_latency")
                .description("秒杀接口响应时间")
                .tag("application", "seckill-system")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(registry);

        // 队列大小
        Gauge.builder("seckill_queue_size", currentQueueSize, AtomicLong::get)
                .description("当前消息队列大小")
                .tag("application", "seckill-system")
                .register(registry);

        // 活跃用户数
        Gauge.builder("seckill_active_users", activeUsers, AtomicLong::get)
                .description("当前活跃用户数")
                .tag("application", "seckill-system")
                .register(registry);
    }

    // ==================== 记录指标 ====================

    /**
     * 记录秒杀请求
     */
    public void recordRequest() {
        seckillRequestsTotal.increment();
    }

    /**
     * 记录秒杀成功
     */
    public void recordSuccess() {
        seckillSuccessTotal.increment();
    }

    /**
     * 记录秒杀失败
     */
    public void recordFail(String reason) {
        seckillFailTotal.increment();

        // 按原因分类
        Counter.builder("seckill_fail_by_reason")
                .tag("reason", reason)
                .register(registry)
                .increment();
    }

    /**
     * 记录售罄
     */
    public void recordSoldOut(Long goodsId) {
        seckillSoldOutTotal.increment();

        Counter.builder("seckill_sold_out_by_goods")
                .tag("goodsId", String.valueOf(goodsId))
                .register(registry)
                .increment();
    }

    /**
     * 记录响应时间
     */
    public void recordLatency(long durationMs) {
        seckillLatency.record(java.time.Duration.ofMillis(durationMs));
    }

    /**
     * 更新队列大小
     */
    public void updateQueueSize(long size) {
        currentQueueSize.set(size);
    }

    /**
     * 更新活跃用户数
     */
    public void updateActiveUsers(long count) {
        activeUsers.set(count);
    }

    // ==================== 获取统计 ====================

    /**
     * 获取秒杀指标统计
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("requestsTotal", seckillRequestsTotal.count());
        stats.put("successTotal", seckillSuccessTotal.count());
        stats.put("failTotal", seckillFailTotal.count());
        stats.put("soldOutTotal", seckillSoldOutTotal.count());

        // 计算成功率
        double total = seckillRequestsTotal.count();
        double successRate = total > 0 ? (seckillSuccessTotal.count() / total) * 100 : 0;
        stats.put("successRate", String.format("%.2f%%", successRate));

        // 延迟统计
        stats.put("latency", Map.of(
                "count", seckillLatency.count(),
                "totalTimeMs", seckillLatency.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS),
                "meanMs", seckillLatency.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
                "maxMs", seckillLatency.max(java.util.concurrent.TimeUnit.MILLISECONDS)));

        // 瞬时值
        stats.put("currentQueueSize", currentQueueSize.get());
        stats.put("activeUsers", activeUsers.get());

        return stats;
    }

    /**
     * 重置统计（用于演示）
     */
    public void reset() {
        // Micrometer 的 Counter 不支持重置，这里只是演示
        currentQueueSize.set(0);
        activeUsers.set(0);
    }
}
