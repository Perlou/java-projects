package com.example.analytics.processor;

import com.example.analytics.config.SparkConfig.SparkSessionWrapper;
import com.example.analytics.model.UserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 窗口分析处理器 - Mock 模式
 * 
 * 使用 Java Stream 模拟 Spark SQL 窗口函数。
 * 
 * 【窗口函数概念 - Spark 知识点】
 * 
 * 窗口函数在一组相关行（窗口）上执行计算，不会减少结果行数
 * 与 GROUP BY 的区别：GROUP BY 会聚合行，窗口函数保留每一行
 * 
 * 窗口定义：
 * - PARTITION BY: 定义分区（类似 GROUP BY）
 * - ORDER BY: 定义窗口内的排序
 * - ROWS/RANGE: 定义窗口范围
 * 
 * 常用窗口函数：
 * - row_number(): 行号
 * - rank(): 排名（有并列）
 * - dense_rank(): 密集排名
 * - lead()/lag(): 前后行数据
 * - sum()/avg() over window: 窗口聚合
 * 
 * @author Java Course
 * 
 */
@Component
public class WindowAnalyzer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(WindowAnalyzer.class);

    private final transient SparkSessionWrapper sparkSession;

    public WindowAnalyzer(SparkSessionWrapper sparkSession) {
        this.sparkSession = sparkSession;
        log.info("WindowAnalyzer 初始化完成 (Mock Mode: {})", sparkSession.isMockMode());
    }

    /**
     * 用户行为排名分析
     * 
     * 【对应 Spark SQL】
     * SELECT userId, actionCount,
     * row_number() OVER (ORDER BY actionCount DESC) as rowNum,
     * rank() OVER (ORDER BY actionCount DESC) as rank,
     * dense_rank() OVER (ORDER BY actionCount DESC) as denseRank
     * FROM user_stats
     */
    public List<Map<String, Object>> analyzeUserRanking(List<UserAction> actions) {
        log.info("分析用户活跃度排名... (模拟窗口函数)");

        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算每个用户的统计
        Map<String, Long> userCounts = actions.stream()
                .filter(a -> a.getUserId() != null)
                .collect(Collectors.groupingBy(UserAction::getUserId, Collectors.counting()));

        // 模拟排名窗口函数
        List<Map.Entry<String, Long>> sorted = userCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        int rowNum = 0;
        int rank = 0;
        int denseRank = 0;
        Long lastCount = null;
        int sameCount = 0;

        for (Map.Entry<String, Long> entry : sorted) {
            rowNum++;

            if (lastCount == null || !entry.getValue().equals(lastCount)) {
                rank = rowNum;
                denseRank++;
                sameCount = 1;
            } else {
                sameCount++;
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", entry.getKey());
            row.put("actionCount", entry.getValue());
            row.put("rowNumber", rowNum);
            row.put("rank", rank);
            row.put("denseRank", denseRank);
            results.add(row);

            lastCount = entry.getValue();
        }

        log.info("用户排名分析完成，共 {} 用户", results.size());
        return results;
    }

    /**
     * 页面跳转分析
     * 
     * 【对应 Spark SQL - lead/lag 函数】
     * SELECT userId, sessionId, pageId, timestamp,
     * LAG(pageId, 1) OVER (PARTITION BY userId, sessionId ORDER BY timestamp) as
     * prevPage,
     * LEAD(pageId, 1) OVER (PARTITION BY userId, sessionId ORDER BY timestamp) as
     * nextPage
     * FROM user_actions
     */
    public List<Map<String, Object>> analyzePageTransitions(List<UserAction> actions) {
        log.info("分析页面跳转路径... (模拟 lead/lag 函数)");

        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        // 按会话分组并排序
        Map<String, List<UserAction>> sessions = actions.stream()
                .filter(a -> a.getSessionId() != null && a.getPageId() != null)
                .sorted(Comparator.comparing(a -> a.getTimestamp() != null ? a.getTimestamp().toString() : ""))
                .collect(Collectors.groupingBy(UserAction::getSessionId));

        // 统计跳转
        Map<String, Long> transitions = new HashMap<>();

        for (List<UserAction> sessionActions : sessions.values()) {
            for (int i = 1; i < sessionActions.size(); i++) {
                String prevPage = sessionActions.get(i - 1).getPageId();
                String currPage = sessionActions.get(i).getPageId();
                String transition = prevPage + " -> " + currPage;
                transitions.merge(transition, 1L, Long::sum);
            }
        }

        // 返回 Top 10 跳转
        List<Map<String, Object>> results = transitions.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    String[] parts = e.getKey().split(" -> ");
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("fromPage", parts[0]);
                    row.put("toPage", parts[1]);
                    row.put("count", e.getValue());
                    return row;
                })
                .collect(Collectors.toList());

        log.info("页面跳转分析完成，Top 10 跳转路径");
        return results;
    }

    /**
     * 滚动窗口聚合
     * 
     * 【对应 Spark SQL - 窗口聚合】
     * SELECT userId, pageId, duration,
     * AVG(duration) OVER (PARTITION BY userId ORDER BY timestamp ROWS BETWEEN 2
     * PRECEDING AND CURRENT ROW) as rollingAvg
     * FROM user_actions
     */
    public List<Map<String, Object>> calculateRollingMetrics(List<UserAction> actions) {
        log.info("计算滚动窗口指标... (模拟窗口聚合)");

        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        // 说明滚动窗口概念
        log.info("【滚动窗口说明】");
        log.info("  ROWS BETWEEN 2 PRECEDING AND CURRENT ROW");
        log.info("  表示: 当前行及前两行（共3行）");

        // 按用户分组
        Map<String, List<UserAction>> userActions = actions.stream()
                .filter(a -> a.getUserId() != null && a.getDuration() != null)
                .sorted(Comparator.comparing(a -> a.getTimestamp() != null ? a.getTimestamp().toString() : ""))
                .collect(Collectors.groupingBy(UserAction::getUserId));

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, List<UserAction>> entry : userActions.entrySet()) {
            List<UserAction> userList = entry.getValue();

            for (int i = 0; i < Math.min(5, userList.size()); i++) {
                UserAction action = userList.get(i);

                // 计算滚动平均（前2行 + 当前行）
                int start = Math.max(0, i - 2);
                double rollingSum = 0;
                int count = 0;
                for (int j = start; j <= i; j++) {
                    rollingSum += userList.get(j).getDuration();
                    count++;
                }
                double rollingAvg = count > 0 ? rollingSum / count : 0;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", action.getUserId());
                row.put("pageId", action.getPageId());
                row.put("duration", action.getDuration());
                row.put("rollingAvgDuration", rollingAvg);
                row.put("rollingCount", count);
                results.add(row);
            }
        }

        return results.stream().limit(20).collect(Collectors.toList());
    }

    /**
     * 累计窗口
     * 
     * 【对应 Spark SQL】
     * SUM(duration) OVER (PARTITION BY userId ORDER BY timestamp ROWS BETWEEN
     * UNBOUNDED PRECEDING AND CURRENT ROW)
     */
    public List<Map<String, Object>> calculateCumulativeMetrics(List<UserAction> actions) {
        log.info("计算累计指标... (模拟累计窗口)");

        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("【累计窗口说明】");
        log.info("  ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW");
        log.info("  表示: 从第一行到当前行的累计");

        Map<String, List<UserAction>> userActions = actions.stream()
                .filter(a -> a.getUserId() != null)
                .sorted(Comparator.comparing(a -> a.getTimestamp() != null ? a.getTimestamp().toString() : ""))
                .collect(Collectors.groupingBy(UserAction::getUserId));

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, List<UserAction>> entry : userActions.entrySet()) {
            List<UserAction> userList = entry.getValue();
            long cumulativeCount = 0;
            long cumulativeDuration = 0;

            for (int i = 0; i < Math.min(5, userList.size()); i++) {
                UserAction action = userList.get(i);
                cumulativeCount++;
                if (action.getDuration() != null) {
                    cumulativeDuration += action.getDuration();
                }

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("userId", action.getUserId());
                row.put("pageId", action.getPageId());
                row.put("cumulativeCount", cumulativeCount);
                row.put("cumulativeDuration", cumulativeDuration);
                results.add(row);
            }
        }

        return results.stream().limit(20).collect(Collectors.toList());
    }

    /**
     * 分组 Top N
     * 
     * 【对应 Spark SQL】
     * SELECT * FROM (
     * SELECT channel, productId, interactions,
     * ROW_NUMBER() OVER (PARTITION BY channel ORDER BY interactions DESC) as rank
     * FROM channel_products
     * ) WHERE rank <= N
     */
    public List<Map<String, Object>> topProductsByChannel(List<UserAction> actions, int topN) {
        log.info("按渠道分析 Top {} 商品... (模拟分组 Top N)", topN);

        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        // 按渠道和商品聚合
        Map<String, Map<String, Long>> channelProducts = actions.stream()
                .filter(a -> a.getChannel() != null && a.getProductId() != null)
                .collect(Collectors.groupingBy(
                        UserAction::getChannel,
                        Collectors.groupingBy(UserAction::getProductId, Collectors.counting())));

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, Map<String, Long>> channelEntry : channelProducts.entrySet()) {
            String channel = channelEntry.getKey();

            // 每个渠道取 Top N
            List<Map.Entry<String, Long>> topProducts = channelEntry.getValue().entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(topN)
                    .collect(Collectors.toList());

            int rank = 0;
            for (Map.Entry<String, Long> product : topProducts) {
                rank++;
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("channel", channel);
                row.put("productId", product.getKey());
                row.put("interactions", product.getValue());
                row.put("rank", rank);
                results.add(row);
            }
        }

        return results;
    }

    /**
     * 演示窗口函数执行计划概念
     */
    public void explainWindowOperations(List<UserAction> actions) {
        log.info("\n=== 窗口函数执行计划说明 ===\n");

        log.info("【Spark 窗口函数执行过程】");
        log.info("");
        log.info("1. PARTITION BY 阶段");
        log.info("   - 数据按分区键重分布 (Shuffle)");
        log.info("   - 相同 Key 的数据在同一分区");
        log.info("");
        log.info("2. ORDER BY 阶段");
        log.info("   - 每个分区内数据排序");
        log.info("   - 为窗口计算做准备");
        log.info("");
        log.info("3. 窗口计算阶段");
        log.info("   - 按窗口范围 (ROWS/RANGE) 计算");
        log.info("   - 应用窗口函数 (row_number, sum 等)");
        log.info("");
        log.info("【执行计划关键节点】");
        log.info("  • Window: 窗口操作");
        log.info("  • Sort: 窗口内排序");
        log.info("  • Exchange: 数据重分布 (Shuffle)");
    }
}
