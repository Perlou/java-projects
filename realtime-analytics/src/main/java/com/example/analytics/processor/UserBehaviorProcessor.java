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
 * 用户行为处理器 - Mock 模式
 * 
 * 展示 Spark 在用户行为分析场景中的典型应用。
 * 
 * @author Java Course
 * @since Phase 22
 */
@Component
public class UserBehaviorProcessor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(UserBehaviorProcessor.class);

    private final transient SparkSessionWrapper sparkSession;

    public UserBehaviorProcessor(SparkSessionWrapper sparkSession) {
        this.sparkSession = sparkSession;
        log.info("UserBehaviorProcessor 初始化完成 (Mock Mode: {})", sparkSession.isMockMode());
    }

    /**
     * 分析用户会话
     */
    public Map<String, Object> analyzeUserSessions(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("分析用户会话...");

        Map<String, Object> sessionStats = new HashMap<>();

        // 总会话数
        long totalSessions = actions.stream()
                .map(UserAction::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        sessionStats.put("totalSessions", totalSessions);

        // 按会话聚合
        Map<String, List<UserAction>> sessions = actions.stream()
                .filter(a -> a.getSessionId() != null)
                .collect(Collectors.groupingBy(UserAction::getSessionId));

        // 平均会话时长和行为数
        double avgDuration = sessions.values().stream()
                .mapToInt(list -> list.stream()
                        .filter(a -> a.getDuration() != null)
                        .mapToInt(UserAction::getDuration)
                        .sum())
                .average()
                .orElse(0);
        sessionStats.put("avgSessionDuration", avgDuration);

        double avgActions = sessions.values().stream()
                .mapToInt(List::size)
                .average()
                .orElse(0);
        sessionStats.put("avgActionsPerSession", avgActions);

        // 跳出率
        long bounces = sessions.values().stream()
                .filter(list -> list.size() == 1)
                .count();
        sessionStats.put("bounceSessions", bounces);
        sessionStats.put("bounceRate",
                totalSessions > 0 ? String.format("%.2f%%", (double) bounces / totalSessions * 100) : "0%");

        log.info("会话分析完成: {}", sessionStats);
        return sessionStats;
    }

    /**
     * 分析用户行为路径
     */
    public List<Map<String, Object>> analyzeUserPaths(List<UserAction> actions, int topN) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("分析用户行为路径...");

        Map<String, List<UserAction>> sessions = actions.stream()
                .filter(a -> a.getSessionId() != null && a.getPageId() != null)
                .sorted(Comparator.comparing(a -> a.getTimestamp() != null ? a.getTimestamp().toString() : ""))
                .collect(Collectors.groupingBy(UserAction::getSessionId));

        // 提取路径
        Map<String, Long> pathCounts = new HashMap<>();
        for (List<UserAction> sessionActions : sessions.values()) {
            if (sessionActions.size() >= 2) {
                String path = sessionActions.stream()
                        .map(UserAction::getPageId)
                        .collect(Collectors.joining(" -> "));
                pathCounts.merge(path, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> results = pathCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("path", e.getKey());
                    row.put("count", e.getValue());
                    row.put("pathLength", e.getKey().split(" -> ").length);
                    return row;
                })
                .collect(Collectors.toList());

        log.info("Top {} 用户路径分析完成", topN);
        return results;
    }

    /**
     * 计算转化漏斗
     */
    public Map<String, Object> calculateConversionFunnel(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("计算转化漏斗...");

        Map<String, Object> funnel = new LinkedHashMap<>();

        long viewers = actions.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.VIEW)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        funnel.put("step1_view", viewers);

        long carters = actions.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.ADD_CART)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        funnel.put("step2_add_cart", carters);

        long purchasers = actions.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.PURCHASE)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        funnel.put("step3_purchase", purchasers);

        if (viewers > 0) {
            funnel.put("view_to_cart_rate", String.format("%.2f%%", (double) carters / viewers * 100));
            funnel.put("view_to_purchase_rate", String.format("%.2f%%", (double) purchasers / viewers * 100));
        }
        if (carters > 0) {
            funnel.put("cart_to_purchase_rate", String.format("%.2f%%", (double) purchasers / carters * 100));
        }

        log.info("漏斗分析: {}", funnel);
        return funnel;
    }

    /**
     * 分析用户活跃度分布
     */
    public Map<String, Long> analyzeUserActivity(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("分析用户活跃度分布...");

        Map<String, Long> userCounts = actions.stream()
                .filter(a -> a.getUserId() != null)
                .collect(Collectors.groupingBy(UserAction::getUserId, Collectors.counting()));

        long lowActivity = userCounts.values().stream().filter(c -> c <= 5).count();
        long mediumActivity = userCounts.values().stream().filter(c -> c > 5 && c <= 20).count();
        long highActivity = userCounts.values().stream().filter(c -> c > 20).count();

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("low (1-5)", lowActivity);
        distribution.put("medium (6-20)", mediumActivity);
        distribution.put("high (21+)", highActivity);

        log.info("用户活跃度分布: {}", distribution);
        return distribution;
    }

    /**
     * 分析热门商品
     */
    public List<Map<String, Object>> analyzeHotProducts(List<UserAction> actions, int topN) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("分析 Top {} 热门商品...", topN);

        Map<String, List<UserAction>> productActions = actions.stream()
                .filter(a -> a.getProductId() != null)
                .collect(Collectors.groupingBy(UserAction::getProductId));

        List<Map<String, Object>> results = productActions.entrySet().stream()
                .map(e -> {
                    String productId = e.getKey();
                    List<UserAction> prodActions = e.getValue();

                    long total = prodActions.size();
                    long views = prodActions.stream()
                            .filter(a -> a.getActionType() == UserAction.ActionType.VIEW)
                            .count();
                    long purchases = prodActions.stream()
                            .filter(a -> a.getActionType() == UserAction.ActionType.PURCHASE)
                            .count();
                    long uniqueUsers = prodActions.stream()
                            .map(UserAction::getUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("productId", productId);
                    row.put("totalInteractions", total);
                    row.put("views", views);
                    row.put("purchases", purchases);
                    row.put("uniqueUsers", uniqueUsers);
                    if (views > 0) {
                        row.put("conversionRate", String.format("%.2f%%", (double) purchases / views * 100));
                    }
                    return row;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("totalInteractions"),
                        (Long) a.get("totalInteractions")))
                .limit(topN)
                .collect(Collectors.toList());

        int rank = 0;
        for (Map<String, Object> row : results) {
            row.put("rank", ++rank);
        }

        log.info("热门商品分析完成");
        return results;
    }

    /**
     * 分析用户设备偏好
     */
    public Map<String, Map<String, Long>> analyzeDevicePreference(List<UserAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return Collections.emptyMap();
        }

        log.info("分析设备偏好...");

        Map<String, List<UserAction>> deviceGroups = actions.stream()
                .filter(a -> a.getDeviceType() != null)
                .collect(Collectors.groupingBy(UserAction::getDeviceType));

        Map<String, Map<String, Long>> results = new LinkedHashMap<>();

        for (Map.Entry<String, List<UserAction>> entry : deviceGroups.entrySet()) {
            List<UserAction> deviceActions = entry.getValue();

            long actionCount = deviceActions.size();
            long totalDuration = deviceActions.stream()
                    .filter(a -> a.getDuration() != null)
                    .mapToLong(UserAction::getDuration)
                    .sum();
            long avgDuration = actionCount > 0 ? totalDuration / actionCount : 0;

            Map<String, Long> stats = new LinkedHashMap<>();
            stats.put("actions", actionCount);
            stats.put("totalDuration", totalDuration);
            stats.put("avgDuration", avgDuration);
            results.put(entry.getKey(), stats);
        }

        log.info("设备偏好分析: {}", results);
        return results;
    }
}
