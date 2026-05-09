package com.example.riskcontrol.processor;

import com.example.riskcontrol.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 窗口聚合器
 * 
 * 【Flink 概念对应】
 * 模拟 Flink 的 Window 机制：
 * 
 * - TumblingWindow: 滚动窗口（不重叠）
 * - SlidingWindow: 滑动窗口（可重叠）
 * - SessionWindow: 会话窗口（按活动间隙）
 * 
 * 窗口函数：
 * - ReduceFunction: 增量聚合
 * - AggregateFunction: 自定义聚合
 * - ProcessWindowFunction: 全量处理
 */
@Component
public class WindowAggregator {

    private static final Logger log = LoggerFactory.getLogger(WindowAggregator.class);

    @Value("${riskcontrol.window.tumbling-seconds:60}")
    private int tumblingWindowSeconds;

    @Value("${riskcontrol.window.sliding-seconds:300}")
    private int slidingWindowSeconds;

    @Value("${riskcontrol.window.sliding-step:60}")
    private int slidingStepSeconds;

    /**
     * 滚动窗口统计结果
     * Key: windowKey (userId_windowStart)
     */
    private final Map<String, WindowStats> tumblingWindowStats = new ConcurrentHashMap<>();

    /**
     * 全局窗口统计
     */
    private final Map<String, WindowStats> globalStats = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("WindowAggregator 初始化完成");
        log.info("【Flink 知识点】窗口配置:");
        log.info("  • TumblingWindow: {} 秒", tumblingWindowSeconds);
        log.info("  • SlidingWindow: {} 秒，步长 {} 秒", slidingWindowSeconds, slidingStepSeconds);
    }

    /**
     * 滚动窗口聚合
     * 
     * 【Flink 实现】
     * stream.keyBy(tx -> tx.getUserId())
     * .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
     * .aggregate(new TransactionAggregator())
     */
    public WindowStats aggregateTumblingWindow(String userId, Transaction tx) {
        // 计算窗口开始时间
        LocalDateTime windowStart = calculateTumblingWindowStart(tx.getEventTime());
        String windowKey = userId + "_" + windowStart.toString();

        WindowStats stats = tumblingWindowStats.computeIfAbsent(windowKey, k -> {
            WindowStats newStats = new WindowStats();
            newStats.setUserId(userId);
            newStats.setWindowStart(windowStart);
            newStats.setWindowEnd(windowStart.plusSeconds(tumblingWindowSeconds));
            newStats.setWindowType("TUMBLING");
            return newStats;
        });

        // 增量聚合（模拟 AggregateFunction.add()）
        stats.addTransaction(tx);

        log.debug("【TumblingWindow】用户 {} 窗口 [{}~{}] 统计: count={}, sum={}",
                userId, stats.getWindowStart(), stats.getWindowEnd(),
                stats.getTransactionCount(), stats.getTotalAmount());

        return stats;
    }

    /**
     * 计算滚动窗口开始时间
     */
    private LocalDateTime calculateTumblingWindowStart(LocalDateTime eventTime) {
        long epochSecond = eventTime.toLocalTime().toSecondOfDay();
        long windowIndex = epochSecond / tumblingWindowSeconds;
        return eventTime.truncatedTo(ChronoUnit.DAYS)
                .plusSeconds(windowIndex * tumblingWindowSeconds);
    }

    /**
     * 滑动窗口聚合
     * 
     * 【Flink 实现】
     * stream.keyBy(tx -> tx.getUserId())
     * .window(SlidingProcessingTimeWindows.of(Time.seconds(300), Time.seconds(60)))
     * .aggregate(new TransactionAggregator())
     */
    public List<WindowStats> aggregateSlidingWindow(String userId, List<Transaction> transactions) {
        List<WindowStats> results = new ArrayList<>();

        if (transactions.isEmpty()) {
            return results;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now;
        LocalDateTime windowStart = now.minusSeconds(slidingWindowSeconds);

        // 过滤窗口内的交易
        List<Transaction> windowTx = transactions.stream()
                .filter(tx -> tx.getEventTime().isAfter(windowStart) &&
                        tx.getEventTime().isBefore(windowEnd))
                .toList();

        if (!windowTx.isEmpty()) {
            WindowStats stats = new WindowStats();
            stats.setUserId(userId);
            stats.setWindowStart(windowStart);
            stats.setWindowEnd(windowEnd);
            stats.setWindowType("SLIDING");

            for (Transaction tx : windowTx) {
                stats.addTransaction(tx);
            }

            results.add(stats);

            log.debug("【SlidingWindow】用户 {} 窗口 [{}~{}] 统计: count={}, sum={}",
                    userId, windowStart, windowEnd,
                    stats.getTransactionCount(), stats.getTotalAmount());
        }

        return results;
    }

    /**
     * 全局统计聚合
     * 
     * 【Flink 实现】
     * stream.keyBy(tx -> tx.getUserId())
     * .window(GlobalWindows.create())
     * .trigger(CountTrigger.of(100))
     * .aggregate(...)
     */
    public WindowStats getGlobalStats(String userId) {
        return globalStats.computeIfAbsent(userId, k -> {
            WindowStats stats = new WindowStats();
            stats.setUserId(userId);
            stats.setWindowType("GLOBAL");
            return stats;
        });
    }

    /**
     * 更新全局统计
     */
    public void updateGlobalStats(String userId, Transaction tx) {
        WindowStats stats = getGlobalStats(userId);
        stats.addTransaction(tx);
    }

    /**
     * 获取所有窗口统计
     */
    public Map<String, Object> getAllWindowStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("tumblingWindows", tumblingWindowStats.size());
        result.put("globalWindows", globalStats.size());

        // 统计各窗口的交易数
        long totalTumblingTx = tumblingWindowStats.values().stream()
                .mapToInt(WindowStats::getTransactionCount)
                .sum();
        result.put("totalTumblingTransactions", totalTumblingTx);

        // 最近窗口统计
        List<Map<String, Object>> recentWindows = tumblingWindowStats.values().stream()
                .sorted(Comparator.comparing(WindowStats::getWindowStart).reversed())
                .limit(10)
                .map(stats -> {
                    Map<String, Object> windowInfo = new HashMap<>();
                    windowInfo.put("userId", stats.getUserId());
                    windowInfo.put("windowStart", stats.getWindowStart());
                    windowInfo.put("windowEnd", stats.getWindowEnd());
                    windowInfo.put("count", stats.getTransactionCount());
                    windowInfo.put("totalAmount", stats.getTotalAmount());
                    return windowInfo;
                })
                .collect(Collectors.toList());
        result.put("recentWindows", recentWindows);

        return result;
    }

    /**
     * 窗口统计结果
     */
    public static class WindowStats {
        private String userId;
        private LocalDateTime windowStart;
        private LocalDateTime windowEnd;
        private String windowType;
        private int transactionCount;
        private BigDecimal totalAmount = BigDecimal.ZERO;
        private BigDecimal maxAmount = BigDecimal.ZERO;
        private BigDecimal minAmount = BigDecimal.valueOf(Long.MAX_VALUE);
        private Set<String> cities = new HashSet<>();
        private Set<String> merchants = new HashSet<>();

        public void addTransaction(Transaction tx) {
            this.transactionCount++;
            this.totalAmount = this.totalAmount.add(tx.getAmount());
            if (tx.getAmount().compareTo(maxAmount) > 0) {
                this.maxAmount = tx.getAmount();
            }
            if (tx.getAmount().compareTo(minAmount) < 0) {
                this.minAmount = tx.getAmount();
            }
            if (tx.getCity() != null) {
                this.cities.add(tx.getCity());
            }
            if (tx.getMerchantId() != null) {
                this.merchants.add(tx.getMerchantId());
            }
        }

        public BigDecimal getAvgAmount() {
            if (transactionCount == 0)
                return BigDecimal.ZERO;
            return totalAmount.divide(BigDecimal.valueOf(transactionCount), 2,
                    java.math.RoundingMode.HALF_UP);
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }

        public void setWindowStart(LocalDateTime windowStart) {
            this.windowStart = windowStart;
        }

        public LocalDateTime getWindowEnd() {
            return windowEnd;
        }

        public void setWindowEnd(LocalDateTime windowEnd) {
            this.windowEnd = windowEnd;
        }

        public String getWindowType() {
            return windowType;
        }

        public void setWindowType(String windowType) {
            this.windowType = windowType;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(int transactionCount) {
            this.transactionCount = transactionCount;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getMaxAmount() {
            return maxAmount;
        }

        public BigDecimal getMinAmount() {
            return minAmount;
        }

        public Set<String> getCities() {
            return cities;
        }

        public Set<String> getMerchants() {
            return merchants;
        }
    }
}
