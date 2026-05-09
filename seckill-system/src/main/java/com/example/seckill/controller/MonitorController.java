package com.example.seckill.controller;

import com.example.seckill.cache.MultiLevelCacheService;
import com.example.seckill.common.Result;
import com.example.seckill.monitor.*;
import com.example.seckill.storage.LSMTreeSimulator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Phase 13: 性能监控 API
 * Phase 17: 增加多级缓存和分布式存储监控
 * 
 * 提供 JVM、缓存、连接池、线程池、SQL 性能的监控接口
 */
@RestController
@RequestMapping("/api/monitor")
@Tag(name = "性能监控", description = "Phase 13 - 性能监控接口")
public class MonitorController {

    private final JvmMonitorService jvmMonitorService;
    private final CacheService cacheService;
    private final ConnectionPoolMonitor connectionPoolMonitor;
    private final ThreadPoolMonitor threadPoolMonitor;
    private final SqlPerformanceService sqlPerformanceService;
    private final PerformanceTestService performanceTestService;
    private final GcLogAnalyzer gcLogAnalyzer;
    private final PerformanceReportService performanceReportService;

    // Phase 17: 多级缓存和分布式存储
    private final MultiLevelCacheService multiLevelCacheService;
    private final LSMTreeSimulator lsmTreeSimulator;

    public MonitorController(JvmMonitorService jvmMonitorService,
            CacheService cacheService,
            ConnectionPoolMonitor connectionPoolMonitor,
            ThreadPoolMonitor threadPoolMonitor,
            SqlPerformanceService sqlPerformanceService,
            PerformanceTestService performanceTestService,
            GcLogAnalyzer gcLogAnalyzer,
            PerformanceReportService performanceReportService,
            MultiLevelCacheService multiLevelCacheService,
            LSMTreeSimulator lsmTreeSimulator) {
        this.jvmMonitorService = jvmMonitorService;
        this.cacheService = cacheService;
        this.connectionPoolMonitor = connectionPoolMonitor;
        this.threadPoolMonitor = threadPoolMonitor;
        this.sqlPerformanceService = sqlPerformanceService;
        this.performanceTestService = performanceTestService;
        this.gcLogAnalyzer = gcLogAnalyzer;
        this.performanceReportService = performanceReportService;
        this.multiLevelCacheService = multiLevelCacheService;
        this.lsmTreeSimulator = lsmTreeSimulator;
    }

    // ==================== JVM 监控 ====================

    @GetMapping("/jvm")
    @Operation(summary = "获取完整 JVM 信息")
    public Result<Map<String, Object>> getJvmInfo() {
        return Result.success(jvmMonitorService.getJvmInfo());
    }

    @GetMapping("/jvm/memory")
    @Operation(summary = "获取内存使用情况")
    public Result<Map<String, Object>> getMemoryInfo() {
        return Result.success(jvmMonitorService.getMemoryInfo());
    }

    @GetMapping("/jvm/gc")
    @Operation(summary = "获取 GC 统计信息")
    public Result<Map<String, Object>> getGcInfo() {
        return Result.success(jvmMonitorService.getGcInfo());
    }

    @GetMapping("/jvm/gc/analysis")
    @Operation(summary = "获取 GC 详细分析报告")
    public Result<Map<String, Object>> getGcAnalysis() {
        return Result.success(gcLogAnalyzer.getGcAnalysisReport());
    }

    @GetMapping("/jvm/threads")
    @Operation(summary = "获取线程信息")
    public Result<Map<String, Object>> getThreadInfo() {
        return Result.success(jvmMonitorService.getThreadInfo());
    }

    @PostMapping("/jvm/gc")
    @Operation(summary = "触发 GC (仅测试用)")
    public Result<Map<String, Object>> triggerGc() {
        return Result.success(jvmMonitorService.triggerGc());
    }

    // ==================== 缓存监控 ====================

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息")
    public Result<Map<String, Object>> getCacheStats() {
        return Result.success(cacheService.getCacheStats());
    }

    @GetMapping("/cache/get")
    @Operation(summary = "多级缓存读取演示")
    public Result<Map<String, Object>> cacheGet(@RequestParam String key) {
        return Result.success(cacheService.getWithMultiLevelCache(key));
    }

    @PostMapping("/cache/stress")
    @Operation(summary = "缓存压力测试")
    public Result<Map<String, Object>> cacheStressTest(
            @RequestParam(defaultValue = "1000") int iterations) {
        return Result.success(cacheService.runCacheStressTest(iterations));
    }

    @DeleteMapping("/cache")
    @Operation(summary = "清除缓存")
    public Result<Map<String, Object>> clearCache(
            @RequestParam(required = false) String key) {
        return Result.success(cacheService.clearCache(key));
    }

    // ==================== 连接池监控 ====================

    @GetMapping("/pool/datasource")
    @Operation(summary = "获取数据库连接池状态")
    public Result<Map<String, Object>> getDataSourcePoolStatus() {
        return Result.success(connectionPoolMonitor.getPoolStatus());
    }

    @GetMapping("/pool/datasource/config")
    @Operation(summary = "获取连接池配置对比")
    public Result<Map<String, Object>> getDataSourcePoolConfig() {
        return Result.success(connectionPoolMonitor.getConfigComparison());
    }

    // ==================== 线程池监控 ====================

    @GetMapping("/pool/thread")
    @Operation(summary = "获取线程池状态")
    public Result<Map<String, Object>> getThreadPoolStatus() {
        return Result.success(threadPoolMonitor.getAllPoolStatus());
    }

    @GetMapping("/pool/thread/recommend")
    @Operation(summary = "获取线程池推荐配置")
    public Result<Map<String, Object>> getThreadPoolRecommendation() {
        return Result.success(threadPoolMonitor.getRecommendedConfig());
    }

    // ==================== SQL 性能监控 ====================

    @GetMapping("/sql/explain")
    @Operation(summary = "执行 SQL EXPLAIN 分析")
    public Result<Map<String, Object>> explainSql(@RequestParam String sql) {
        return Result.success(sqlPerformanceService.explainQuery(sql));
    }

    @GetMapping("/sql/slow")
    @Operation(summary = "获取慢查询统计")
    public Result<Map<String, Object>> getSlowQueryStats() {
        return Result.success(sqlPerformanceService.getSlowQueryStats());
    }

    @GetMapping("/sql/index/{tableName}")
    @Operation(summary = "获取表索引建议")
    public Result<Map<String, Object>> getIndexSuggestions(@PathVariable String tableName) {
        return Result.success(sqlPerformanceService.getIndexSuggestions(tableName));
    }

    @GetMapping("/sql/mysql-variables")
    @Operation(summary = "获取 MySQL 性能变量")
    public Result<Map<String, Object>> getMySqlVariables() {
        return Result.success(sqlPerformanceService.getMySqlPerformanceVariables());
    }

    // ==================== 压力测试 ====================

    @PostMapping("/stress/http")
    @Operation(summary = "HTTP 压力测试")
    public Result<Map<String, Object>> httpStressTest(
            @RequestParam String url,
            @RequestParam(defaultValue = "10") int concurrent,
            @RequestParam(defaultValue = "100") int requests,
            @RequestParam(defaultValue = "GET") String method) {
        return Result.success(performanceTestService.runHttpStressTest(url, concurrent, requests, method));
    }

    @PostMapping("/stress/memory")
    @Operation(summary = "内存压力测试")
    public Result<Map<String, Object>> memoryStressTest(
            @RequestParam(defaultValue = "100") int sizeMb,
            @RequestParam(defaultValue = "5") int durationSeconds) {
        return Result.success(performanceTestService.runMemoryStressTest(sizeMb, durationSeconds));
    }

    @PostMapping("/stress/cpu")
    @Operation(summary = "CPU 压力测试")
    public Result<Map<String, Object>> cpuStressTest(
            @RequestParam(defaultValue = "4") int threads,
            @RequestParam(defaultValue = "5") int durationSeconds) {
        return Result.success(performanceTestService.runCpuStressTest(threads, durationSeconds));
    }

    // ==================== 综合报告 ====================

    @GetMapping("/dashboard")
    @Operation(summary = "获取监控面板数据")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("jvm_memory", jvmMonitorService.getMemoryInfo());
        dashboard.put("jvm_gc", jvmMonitorService.getGcInfo());
        dashboard.put("cache_stats", cacheService.getCacheStats());
        dashboard.put("connection_pool", connectionPoolMonitor.getPoolStatus());
        dashboard.put("thread_pools", threadPoolMonitor.getAllPoolStatus());
        // Phase 17: 多级缓存统计
        dashboard.put("multilevel_cache", multiLevelCacheService.getStats());
        return Result.success(dashboard);
    }

    @GetMapping("/report")
    @Operation(summary = "生成完整性能报告")
    public Result<Map<String, Object>> getPerformanceReport() {
        return Result.success(performanceReportService.generateFullReport());
    }

    // ==================== Phase 17: 分布式存储监控 ====================

    @GetMapping("/cache/multilevel")
    @Operation(summary = "获取多级缓存统计", description = "Phase 17 - L1 Caffeine + L2 Redis 多级缓存命中率")
    public Result<Map<String, Object>> getMultiLevelCacheStats() {
        return Result.success("多级缓存统计", multiLevelCacheService.getStats());
    }

    @PostMapping("/cache/multilevel/clear")
    @Operation(summary = "清空多级缓存 L1 本地缓存")
    public Result<String> clearMultiLevelCache() {
        multiLevelCacheService.clearAll();
        return Result.success("L1 本地缓存已清空", null);
    }

    @GetMapping("/storage/lsm")
    @Operation(summary = "获取 LSM-Tree 模拟器状态", description = "Phase 17 - 查看 MemTable、SSTables 各层状态")
    public Result<Map<String, Object>> getLSMTreeStatus() {
        return Result.success("LSM-Tree 状态", lsmTreeSimulator.getStatus());
    }

    @GetMapping("/storage/stats")
    @Operation(summary = "分布式存储架构状态", description = "Phase 17 - 综合存储架构状态")
    public Result<Map<String, Object>> getStorageStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("multiLevelCache", multiLevelCacheService.getStats());
        stats.put("lsmTree", lsmTreeSimulator.getStatus());
        stats.put("architecture", Map.of(
                "L1_Cache", "Caffeine (本地内存)",
                "L2_Cache", "Redis (分布式)",
                "Database", "MySQL (持久化)",
                "MessageQueue", "RabbitMQ (异步处理)",
                "pattern", "Cache-Aside + 多级缓存"));
        return Result.success("分布式存储架构状态", stats);
    }
}
