package com.example.analytics.processor;

import com.example.analytics.config.SparkConfig.SparkSessionWrapper;
import com.example.analytics.model.MetricResult;
import com.example.analytics.model.UserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 实时聚合处理器 - Mock 模式
 * 
 * 使用 Java Stream API 模拟 Spark RDD 操作。
 * 
 * 【RDD 核心概念 - 用注释说明】
 * 
 * 1. RDD (Resilient Distributed Dataset) 弹性分布式数据集
 * - 不可变的分布式数据集合
 * - 支持并行处理
 * - 具有容错能力（通过血统 Lineage 重建）
 * 
 * 2. 算子分类
 * - 转换算子 (Transformation): map, filter, flatMap, groupBy 等
 * 惰性求值，只记录转换逻辑，不立即执行
 * - 行动算子 (Action): collect, count, reduce, saveAsTextFile 等
 * 触发实际计算
 * 
 * 3. 依赖类型
 * - 窄依赖 (Narrow): 父 RDD 的每个分区最多被一个子 RDD 分区使用
 * 如 map, filter - 可以 Pipeline 执行
 * - 宽依赖 (Wide): 父 RDD 的分区被多个子 RDD 分区使用
 * 如 groupByKey, reduceByKey - 需要 Shuffle
 * 
 * @author Java Course
 * @since Phase 22
 */
@Component
public class RealTimeAggregator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(RealTimeAggregator.class);

    private final transient SparkSessionWrapper sparkSession;

    public RealTimeAggregator(SparkSessionWrapper sparkSession) {
        this.sparkSession = sparkSession;
        log.info("RealTimeAggregator 初始化完成 (Mock Mode: {})", sparkSession.isMockMode());
    }

    /**
     * 演示基本 RDD 操作
     * 
     * 【转换算子链】
     * map -> filter -> 这是一个典型的 Pipeline，窄依赖
     * 
     * 【Spark 等效代码】
     * val rdd = sc.parallelize(data, 4)
     * val squared = rdd.map(x => x * x)
     * val filtered = squared.filter(x => x > 25)
     */
    public void demonstrateBasicRddOperations() {
        log.info("\n=== 演示基本 RDD 操作 (Mock) ===\n");

        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        log.info("原始数据: {}", data);
        log.info("模拟分区数: 4");

        // 模拟 map - 转换每个元素（窄依赖）
        List<Integer> squaredResult = data.stream()
                .map(x -> x * x)
                .collect(Collectors.toList());
        log.info("map(x -> x*x) 结果: {}", squaredResult);

        // 模拟 filter - 过滤元素（窄依赖）
        List<Integer> filteredResult = squaredResult.stream()
                .filter(x -> x > 25)
                .collect(Collectors.toList());
        log.info("filter(x -> x > 25) 结果: {}", filteredResult);

        // 模拟 reduce - 聚合所有元素（行动算子）
        Integer sum = data.stream().reduce(0, Integer::sum);
        log.info("reduce(sum) 结果: {}", sum);

        // 模拟 count - 计数（行动算子）
        long count = data.size();
        log.info("count 结果: {}", count);

        log.info("\n【Spark 知识点】");
        log.info("  • map/filter 是窄依赖，可以 Pipeline 执行");
        log.info("  • reduce/count 是行动算子，触发实际计算");
        log.info("  • 在真实 Spark 中，以上操作会分布在多个节点执行");
    }

    /**
     * 使用模拟 RDD 计算 UV (独立用户数)
     * 
     * 【对应 Spark 代码】
     * rdd.map(_.getUserId)
     * .filter(_ != null)
     * .distinct() // 宽依赖，需要 Shuffle
     * .count()
     */
    public long calculateUv(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return 0;
        }

        long uv = actions.stream()
                .map(UserAction::getUserId) // 窄依赖：提取 userId
                .filter(Objects::nonNull) // 窄依赖：过滤 null
                .distinct() // 这里模拟 Shuffle
                .count(); // 行动算子

        log.info("UV 计算完成: {} (模拟 distinct 需要 Shuffle)", uv);
        return uv;
    }

    /**
     * 统计各行为类型的数量
     * 
     * 【reduceByKey vs groupByKey - Spark 知识点】
     * 
     * reduceByKey: 先在 Map 端局部聚合，再 Shuffle，更高效
     * groupByKey: 直接 Shuffle 所有数据，可能导致内存问题
     * 
     * 【对应 Spark 代码】
     * rdd.filter(_.getActionType != null)
     * .map(a => (a.getActionType.name, 1L))
     * .reduceByKey(_ + _)
     * .collect()
     */
    public Map<String, Long> countByActionType(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Long> result = actions.stream()
                .filter(a -> a.getActionType() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getActionType().name(),
                        Collectors.counting()));

        log.info("行为类型统计: {} (模拟 reduceByKey)", result);
        return result;
    }

    /**
     * 按页面统计访问量
     * 
     * 【对应 Spark 代码】
     * rdd.filter(_.getPageId != null)
     * .map(a => (a.getPageId, 1L))
     * .reduceByKey(_ + _)
     * .sortBy(_._2, ascending = false)
     * .collect()
     */
    public List<MetricResult> countByPage(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Long> pageCounts = actions.stream()
                .filter(a -> a.getPageId() != null)
                .collect(Collectors.groupingBy(
                        UserAction::getPageId,
                        Collectors.counting()));

        List<MetricResult> results = pageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> MetricResult.of("page_views", e.getValue(), "pageId", e.getKey()))
                .collect(Collectors.toList());

        log.info("页面统计完成，共 {} 个页面", results.size());
        return results;
    }

    /**
     * 演示 flatMap 操作
     * 
     * 【flatMap - Spark 知识点】
     * 将一个元素映射为多个元素（一对多映射）
     * 类似于先 map 再 flatten
     * 
     * 【对应 Spark 代码】
     * rdd.flatMap(a => {
     * val ids = new ListBuffer[String]()
     * if (a.getUserId != null) ids += "user:" + a.getUserId
     * if (a.getSessionId != null) ids += "session:" + a.getSessionId
     * ids
     * }).distinct().collect()
     */
    public List<String> extractAllUserIds(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> allIds = actions.stream()
                .flatMap(action -> {
                    List<String> ids = new ArrayList<>();
                    if (action.getUserId() != null)
                        ids.add("user:" + action.getUserId());
                    if (action.getSessionId() != null)
                        ids.add("session:" + action.getSessionId());
                    return ids.stream();
                })
                .distinct()
                .collect(Collectors.toList());

        log.info("提取的 ID 数量: {} (模拟 flatMap)", allIds.size());
        return allIds;
    }

    /**
     * 演示 aggregate 操作
     * 
     * 【aggregate - Spark 知识点】
     * 更灵活的聚合操作，支持：
     * - 初始值 (zeroValue)
     * - 分区内聚合函数 (seqOp)
     * - 分区间合并函数 (combOp)
     */
    public Map<String, Object> aggregateStats(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        // 使用 reduce 模拟 aggregate
        long count = actions.stream()
                .filter(a -> a.getDuration() != null)
                .count();

        long totalDuration = actions.stream()
                .filter(a -> a.getDuration() != null)
                .mapToLong(UserAction::getDuration)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("count", count);
        stats.put("totalDuration", totalDuration);
        stats.put("avgDuration", count > 0 ? (double) totalDuration / count : 0.0);

        log.info("聚合统计: {} (模拟 aggregate)", stats);
        return stats;
    }

    /**
     * 演示缓存/持久化概念
     * 
     * 【RDD 持久化 - Spark 知识点】
     * cache(): 等同于 persist(StorageLevel.MEMORY_ONLY)
     * persist(): 可指定存储级别
     * 
     * 存储级别：
     * - MEMORY_ONLY: 仅内存
     * - MEMORY_AND_DISK: 内存+磁盘
     * - MEMORY_ONLY_SER: 序列化存储，更省空间
     * - DISK_ONLY: 仅磁盘
     */
    public void demonstrateCaching(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        log.info("\n=== 演示 RDD 缓存概念 ===\n");
        log.info("【Spark 缓存说明】");
        log.info("  • rdd.cache() 将 RDD 缓存到内存");
        log.info("  • 适用于需要多次使用的 RDD");
        log.info("  • 可使用 persist() 指定存储级别");
        log.info("  • 常用级别: MEMORY_ONLY, MEMORY_AND_DISK");
        log.info("  • 不再需要时调用 unpersist() 释放");
    }

    /**
     * 演示分区操作概念
     * 
     * 【分区控制 - Spark 知识点】
     * - repartition(n): 重新分区，总是 Shuffle
     * - coalesce(n): 减少分区，可避免 Shuffle
     */
    public void demonstratePartitioning(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }

        log.info("\n=== 演示分区操作概念 ===\n");
        log.info("数据量: {} 条", actions.size());
        log.info("");
        log.info("【Spark 分区说明】");
        log.info("  • 分区数决定并行度");
        log.info("  • repartition(n): 增加/减少分区，总是触发 Shuffle");
        log.info("  • coalesce(n): 只能减少分区，可避免 Shuffle");
        log.info("  • 建议分区数 = 核心数 × 2~3");
    }
}
