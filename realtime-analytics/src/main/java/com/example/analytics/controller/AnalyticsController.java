package com.example.analytics.controller;

import com.example.analytics.model.AnalyticsReport;
import com.example.analytics.model.MetricResult;
import com.example.analytics.processor.RealTimeAggregator;
import com.example.analytics.processor.UserBehaviorProcessor;
import com.example.analytics.processor.WindowAnalyzer;
import com.example.analytics.service.DataIngestionService;
import com.example.analytics.service.SparkBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分析控制器
 * 
 * 提供数据分析的 REST API
 * 
 * @author Java Course
 * 
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "数据分析", description = "Spark 数据分析 API (Mock 模式)")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final SparkBatchService sparkBatchService;
    private final RealTimeAggregator realTimeAggregator;
    private final WindowAnalyzer windowAnalyzer;
    private final UserBehaviorProcessor userBehaviorProcessor;
    private final DataIngestionService dataIngestionService;

    public AnalyticsController(SparkBatchService sparkBatchService,
            RealTimeAggregator realTimeAggregator,
            WindowAnalyzer windowAnalyzer,
            UserBehaviorProcessor userBehaviorProcessor,
            DataIngestionService dataIngestionService) {
        this.sparkBatchService = sparkBatchService;
        this.realTimeAggregator = realTimeAggregator;
        this.windowAnalyzer = windowAnalyzer;
        this.userBehaviorProcessor = userBehaviorProcessor;
        this.dataIngestionService = dataIngestionService;
    }

    // ========== 基础指标分析 ==========

    @GetMapping("/pvuv")
    @Operation(summary = "计算 PV/UV", description = "使用模拟 Spark 计算页面浏览量和独立访客数")
    public ResponseEntity<Map<String, Object>> calculatePvUv() {
        log.info("API: 计算 PV/UV");

        Map<String, Long> metrics = sparkBatchService.calculatePvUv();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mode", "mock");
        response.put("metrics", metrics);
        response.put("dataCount", dataIngestionService.getDataCount());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/page-views")
    @Operation(summary = "页面访问分析", description = "模拟 Spark SQL 分析各页面访问量")
    public ResponseEntity<Map<String, Object>> analyzePageViews() {
        log.info("API: 页面访问分析");

        List<MetricResult> results = sparkBatchService.analyzePageViews();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mode", "mock");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/device-analysis")
    @Operation(summary = "设备分析", description = "按设备类型分析用户行为")
    public ResponseEntity<Map<String, Object>> analyzeByDevice() {
        log.info("API: 设备分析");

        List<MetricResult> results = sparkBatchService.analyzeByDevice();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/funnel")
    @Operation(summary = "转化漏斗", description = "分析用户转化漏斗")
    public ResponseEntity<Map<String, Object>> analyzeFunnel() {
        log.info("API: 转化漏斗分析");

        Map<String, Object> funnel = sparkBatchService.analyzeFunnel();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("funnel", funnel);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-products")
    @Operation(summary = "热门商品", description = "分析 Top N 热门商品")
    public ResponseEntity<Map<String, Object>> analyzeTopProducts(
            @Parameter(description = "Top N 数量") @RequestParam(defaultValue = "10") int topN) {
        log.info("API: Top {} 热门商品分析", topN);

        List<MetricResult> results = sparkBatchService.analyzeTopProducts(topN);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("topN", topN);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    // ========== 完整报告 ==========

    @GetMapping("/report")
    @Operation(summary = "生成分析报告", description = "生成完整的用户行为分析报告")
    public ResponseEntity<AnalyticsReport> generateReport() {
        log.info("API: 生成分析报告");

        AnalyticsReport report = sparkBatchService.generateReport();

        return ResponseEntity.ok(report);
    }

    // ========== RDD 操作演示 ==========

    @PostMapping("/demo/rdd-basics")
    @Operation(summary = "RDD 基础演示", description = "演示 Spark RDD 基本操作概念（查看控制台输出）")
    public ResponseEntity<Map<String, Object>> demoRddBasics() {
        log.info("API: 演示 RDD 基础操作");

        realTimeAggregator.demonstrateBasicRddOperations();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("mode", "mock");
        response.put("message", "RDD 基础操作演示完成，请查看控制台输出了解 Spark 概念");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rdd/aggregate")
    @Operation(summary = "RDD 聚合统计", description = "模拟 RDD aggregate 操作进行统计")
    public ResponseEntity<Map<String, Object>> rddAggregate() {
        log.info("API: RDD 聚合统计");

        Map<String, Object> stats = realTimeAggregator.aggregateStats(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rdd/action-counts")
    @Operation(summary = "按行为类型统计", description = "模拟 RDD reduceByKey 统计各行为类型")
    public ResponseEntity<Map<String, Object>> rddActionCounts() {
        log.info("API: 按行为类型统计");

        Map<String, Long> counts = realTimeAggregator.countByActionType(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("counts", counts);

        return ResponseEntity.ok(response);
    }

    // ========== 窗口函数演示 ==========

    @GetMapping("/window/user-ranking")
    @Operation(summary = "用户排名", description = "模拟窗口函数分析用户活跃度排名")
    public ResponseEntity<Map<String, Object>> userRanking() {
        log.info("API: 用户排名分析");

        List<Map<String, Object>> rankings = windowAnalyzer.analyzeUserRanking(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rankings", rankings);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/window/page-transitions")
    @Operation(summary = "页面跳转分析", description = "模拟 lead/lag 分析页面跳转路径")
    public ResponseEntity<Map<String, Object>> pageTransitions() {
        log.info("API: 页面跳转分析");

        List<Map<String, Object>> transitions = windowAnalyzer
                .analyzePageTransitions(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transitions", transitions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/window/channel-top-products")
    @Operation(summary = "渠道热门商品", description = "按渠道分析 Top N 商品")
    public ResponseEntity<Map<String, Object>> channelTopProducts(
            @RequestParam(defaultValue = "3") int topN) {
        log.info("API: 渠道 Top {} 商品分析", topN);

        List<Map<String, Object>> results = windowAnalyzer.topProductsByChannel(dataIngestionService.getAllData(),
                topN);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    // ========== 用户行为分析 ==========

    @GetMapping("/behavior/sessions")
    @Operation(summary = "会话分析", description = "分析用户会话数据")
    public ResponseEntity<Map<String, Object>> analyzeSessions() {
        log.info("API: 会话分析");

        Map<String, Object> sessionStats = userBehaviorProcessor.analyzeUserSessions(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sessionStats", sessionStats);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/behavior/user-paths")
    @Operation(summary = "用户路径分析", description = "分析常见的用户行为路径")
    public ResponseEntity<Map<String, Object>> analyzeUserPaths(
            @RequestParam(defaultValue = "10") int topN) {
        log.info("API: 用户路径分析");

        List<Map<String, Object>> paths = userBehaviorProcessor.analyzeUserPaths(dataIngestionService.getAllData(),
                topN);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("paths", paths);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/behavior/activity-distribution")
    @Operation(summary = "用户活跃度分布", description = "分析用户活跃度分布")
    public ResponseEntity<Map<String, Object>> analyzeActivityDistribution() {
        log.info("API: 用户活跃度分布分析");

        Map<String, Long> distribution = userBehaviorProcessor.analyzeUserActivity(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("distribution", distribution);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/behavior/hot-products")
    @Operation(summary = "热门商品详细分析", description = "分析热门商品的详细指标")
    public ResponseEntity<Map<String, Object>> analyzeHotProducts(
            @RequestParam(defaultValue = "10") int topN) {
        log.info("API: 热门商品详细分析");

        List<Map<String, Object>> products = userBehaviorProcessor.analyzeHotProducts(dataIngestionService.getAllData(),
                topN);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("products", products);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/behavior/device-preference")
    @Operation(summary = "设备偏好分析", description = "分析用户的设备使用偏好")
    public ResponseEntity<Map<String, Object>> analyzeDevicePreference() {
        log.info("API: 设备偏好分析");

        Map<String, Map<String, Long>> preference = userBehaviorProcessor
                .analyzeDevicePreference(dataIngestionService.getAllData());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("devicePreference", preference);

        return ResponseEntity.ok(response);
    }
}
