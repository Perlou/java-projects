package com.example.analytics.service;

import com.example.analytics.config.SparkConfig.SparkSessionWrapper;
import com.example.analytics.model.AnalyticsReport;
import com.example.analytics.model.MetricResult;
import com.example.analytics.model.UserAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spark 批处理服务 - Mock 模式
 * 
 * 使用 Java Stream API 模拟 Spark DataFrame/SQL 操作，
 * 演示 Spark 的核心概念和处理逻辑。
 * 
 * 【模拟说明】
 * 由于 Spark 3.5 与 Java 18+ 不兼容，本服务使用 Java Stream API
 * 模拟 Spark 的处理逻辑。虽然不是真正的分布式处理，但展示了相同的
 * 数据处理模式和概念。
 * 
 * 【对应 Spark 概念】
 * - Stream.map() → Spark map()
 * - Stream.filter() → Spark filter()
 * - Collectors.groupingBy() → Spark groupBy()
 * - Collectors.counting() → Spark count()
 * 
 * @author Java Course
 * 
 */
@Service
public class SparkBatchService {

    private static final Logger log = LoggerFactory.getLogger(SparkBatchService.class);

    private final SparkSessionWrapper sparkSession;
    private final DataIngestionService dataIngestionService;

    public SparkBatchService(SparkSessionWrapper sparkSession,
            DataIngestionService dataIngestionService) {
        this.sparkSession = sparkSession;
        this.dataIngestionService = dataIngestionService;

        log.info("SparkBatchService 初始化完成 (Mock Mode: {})", sparkSession.isMockMode());
    }

    /**
     * 计算 PV/UV 指标
     * 
     * 【Spark 对应操作】
     * df.agg(
     * count("id").as("pv"),
     * countDistinct("userId").as("uv")
     * )
     */
    public Map<String, Long> calculatePvUv() {
        log.info("计算 PV/UV 指标... (模拟 Spark DataFrame agg 操作)");
        long startTime = System.currentTimeMillis();

        List<UserAction> data = dataIngestionService.getAllData();
        if (data.isEmpty()) {
            log.warn("无数据可分析");
            return Map.of("pv", 0L, "uv", 0L, "sessions", 0L);
        }

        // 模拟 Spark: count("id") as PV
        long pv = data.size();

        // 模拟 Spark: countDistinct("userId") as UV
        long uv = data.stream()
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // 模拟 Spark: countDistinct("sessionId") as Sessions
        long sessions = data.stream()
                .map(UserAction::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Long> metrics = new HashMap<>();
        metrics.put("pv", pv);
        metrics.put("uv", uv);
        metrics.put("sessions", sessions);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("PV/UV 计算完成，耗时 {} ms，PV={}, UV={}, Sessions={}",
                elapsed, pv, uv, sessions);

        return metrics;
    }

    /**
     * 按页面统计访问量
     * 
     * 【对应 Spark SQL】
     * SELECT pageId, COUNT(*) as viewCount, COUNT(DISTINCT userId) as uniqueUsers
     * FROM user_actions
     * WHERE actionType = 'VIEW'
     * GROUP BY pageId
     * ORDER BY viewCount DESC
     */
    public List<MetricResult> analyzePageViews() {
        log.info("分析页面访问量... (模拟 Spark SQL GROUP BY)");

        List<UserAction> data = dataIngestionService.getAllData();
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        // 模拟 Spark: filter + groupBy + agg + orderBy
        Map<String, List<UserAction>> pageGroups = data.stream()
                .filter(a -> a.getPageId() != null)
                .filter(a -> a.getActionType() == UserAction.ActionType.VIEW)
                .collect(Collectors.groupingBy(UserAction::getPageId));

        List<MetricResult> metrics = pageGroups.entrySet().stream()
                .map(entry -> {
                    String pageId = entry.getKey();
                    List<UserAction> actions = entry.getValue();

                    long viewCount = actions.size();
                    long uniqueUsers = actions.stream()
                            .map(UserAction::getUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    double avgDuration = actions.stream()
                            .filter(a -> a.getDuration() != null)
                            .mapToInt(UserAction::getDuration)
                            .average()
                            .orElse(0);

                    MetricResult result = MetricResult.of("page_views", viewCount, "pageId", pageId);
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("pageType", classifyPage(pageId));
                    extra.put("uniqueUsers", uniqueUsers);
                    extra.put("avgDuration", avgDuration);
                    result.setExtra(extra);
                    return result;
                })
                .sorted((a, b) -> Long.compare((Long) b.getValue(), (Long) a.getValue()))
                .collect(Collectors.toList());

        log.info("页面访问分析完成，共 {} 个页面", metrics.size());
        return metrics;
    }

    /**
     * 按设备类型分析
     * 
     * 【对应 Spark UDF + SQL】
     * SELECT classifyDevice(deviceType) as device, COUNT(*) as actions
     * FROM user_actions
     * GROUP BY classifyDevice(deviceType)
     */
    public List<MetricResult> analyzeByDevice() {
        log.info("按设备类型分析... (模拟 Spark UDF)");

        List<UserAction> data = dataIngestionService.getAllData();
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        // 模拟 UDF: classifyDevice
        Map<String, List<UserAction>> deviceGroups = data.stream()
                .filter(a -> a.getDeviceType() != null)
                .collect(Collectors.groupingBy(a -> classifyDevice(a.getDeviceType())));

        List<MetricResult> metrics = deviceGroups.entrySet().stream()
                .map(entry -> {
                    String device = entry.getKey();
                    List<UserAction> actions = entry.getValue();

                    long actionCount = actions.size();
                    long users = actions.stream()
                            .map(UserAction::getUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    long purchases = actions.stream()
                            .filter(a -> a.getActionType() == UserAction.ActionType.PURCHASE)
                            .count();

                    MetricResult result = MetricResult.of("device_analysis", actionCount, "device", device);
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("users", users);
                    extra.put("purchases", purchases);
                    result.setExtra(extra);
                    return result;
                })
                .sorted((a, b) -> Long.compare((Long) b.getValue(), (Long) a.getValue()))
                .collect(Collectors.toList());

        return metrics;
    }

    /**
     * 转化漏斗分析
     */
    public Map<String, Object> analyzeFunnel() {
        log.info("分析转化漏斗...");

        List<UserAction> data = dataIngestionService.getAllData();
        if (data.isEmpty()) {
            return Collections.emptyMap();
        }

        // 计算各步骤的独立用户数
        long viewers = data.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.VIEW)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long carters = data.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.ADD_CART)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long purchasers = data.stream()
                .filter(a -> a.getActionType() == UserAction.ActionType.PURCHASE)
                .map(UserAction::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Object> funnel = new LinkedHashMap<>();
        funnel.put("VIEW", viewers);
        funnel.put("ADD_CART", carters);
        funnel.put("PURCHASE", purchasers);

        if (viewers > 0) {
            funnel.put("view_to_cart_rate", String.format("%.2f%%", (double) carters / viewers * 100));
            funnel.put("view_to_purchase_rate", String.format("%.2f%%", (double) purchasers / viewers * 100));
        }
        if (carters > 0) {
            funnel.put("cart_to_purchase_rate", String.format("%.2f%%", (double) purchasers / carters * 100));
        }

        log.info("漏斗分析完成: {}", funnel);
        return funnel;
    }

    /**
     * 热门商品分析
     */
    public List<MetricResult> analyzeTopProducts(int topN) {
        log.info("分析 Top {} 热门商品...", topN);

        List<UserAction> data = dataIngestionService.getAllData();
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<UserAction>> productGroups = data.stream()
                .filter(a -> a.getProductId() != null)
                .collect(Collectors.groupingBy(UserAction::getProductId));

        List<MetricResult> metrics = productGroups.entrySet().stream()
                .map(entry -> {
                    String productId = entry.getKey();
                    List<UserAction> actions = entry.getValue();

                    long interactions = actions.size();
                    long uniqueUsers = actions.stream()
                            .map(UserAction::getUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .count();
                    long purchases = actions.stream()
                            .filter(a -> a.getActionType() == UserAction.ActionType.PURCHASE)
                            .count();
                    long views = actions.stream()
                            .filter(a -> a.getActionType() == UserAction.ActionType.VIEW)
                            .count();

                    MetricResult result = MetricResult.of("top_product", interactions, "productId", productId);
                    Map<String, Object> extra = new HashMap<>();
                    extra.put("uniqueUsers", uniqueUsers);
                    extra.put("purchases", purchases);
                    extra.put("views", views);
                    result.setExtra(extra);
                    return result;
                })
                .sorted((a, b) -> Long.compare((Long) b.getValue(), (Long) a.getValue()))
                .limit(topN)
                .collect(Collectors.toList());

        return metrics;
    }

    /**
     * 生成完整分析报告
     */
    public AnalyticsReport generateReport() {
        log.info("生成完整分析报告...");
        long startTime = System.currentTimeMillis();

        AnalyticsReport report = new AnalyticsReport();
        report.setReportId(UUID.randomUUID().toString());
        report.setReportName("用户行为分析报告 (Mock Mode)");
        report.setReportType(AnalyticsReport.ReportType.REALTIME);
        report.setEndTime(LocalDateTime.now());
        report.setStartTime(LocalDateTime.now().minusHours(1));

        // 核心指标
        Map<String, Long> pvuv = calculatePvUv();
        AnalyticsReport.CoreMetrics coreMetrics = new AnalyticsReport.CoreMetrics();
        coreMetrics.setPv(pvuv.get("pv"));
        coreMetrics.setUv(pvuv.get("uv"));
        coreMetrics.setSessions(pvuv.get("sessions"));
        report.setCoreMetrics(coreMetrics);

        // 维度分析
        Map<String, List<MetricResult>> dimensionAnalysis = new HashMap<>();
        dimensionAnalysis.put("page", analyzePageViews());
        dimensionAnalysis.put("device", analyzeByDevice());
        dimensionAnalysis.put("topProducts", analyzeTopProducts(10));
        report.setDimensionAnalysis(dimensionAnalysis);

        // 处理信息
        AnalyticsReport.ProcessingInfo processingInfo = new AnalyticsReport.ProcessingInfo();
        processingInfo.setRecordCount((long) dataIngestionService.getDataCount());
        processingInfo.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        report.setProcessingInfo(processingInfo);

        log.info("报告生成完成，耗时 {} ms", processingInfo.getProcessingTimeMs());
        return report;
    }

    // UDF 模拟：设备分类
    private String classifyDevice(String device) {
        if (device == null)
            return "Unknown";
        return switch (device.toLowerCase()) {
            case "pc", "desktop" -> "Desktop";
            case "mobile", "phone" -> "Mobile";
            case "tablet", "ipad" -> "Tablet";
            default -> "Other";
        };
    }

    // UDF 模拟：页面分类
    private String classifyPage(String pageId) {
        if (pageId == null)
            return "Other";
        if (pageId.contains("product"))
            return "Product";
        if (pageId.equals("home"))
            return "Home";
        if (pageId.contains("cart") || pageId.contains("checkout"))
            return "Checkout";
        if (pageId.contains("search"))
            return "Search";
        return "Other";
    }
}
