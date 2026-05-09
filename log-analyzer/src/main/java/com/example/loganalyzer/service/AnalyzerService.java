package com.example.loganalyzer.service;

import com.example.loganalyzer.mapreduce.*;
import com.example.loganalyzer.model.*;
import com.example.loganalyzer.util.LogGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 日志分析服务
 * 
 * 提供 PV/UV 统计、TopN 分析、用户路径分析等功能
 */
@Service
public class AnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(AnalyzerService.class);

    private final MapReduceEngine mapReduceEngine;
    private final LogGenerator logGenerator;
    private final PvUvMapper pvUvMapper;
    private final PvUvReducer pvUvReducer;
    private final TopNMapper topNMapper;
    private final TopNReducer topNReducer;

    // 内存中存储的日志数据
    private List<LogEntry> logEntries = new ArrayList<>();

    public AnalyzerService(MapReduceEngine mapReduceEngine, LogGenerator logGenerator,
            PvUvMapper pvUvMapper, PvUvReducer pvUvReducer,
            TopNMapper topNMapper, TopNReducer topNReducer) {
        this.mapReduceEngine = mapReduceEngine;
        this.logGenerator = logGenerator;
        this.pvUvMapper = pvUvMapper;
        this.pvUvReducer = pvUvReducer;
        this.topNMapper = topNMapper;
        this.topNReducer = topNReducer;
    }

    /**
     * 生成模拟日志数据
     */
    public int generateLogs(int count) {
        logEntries = logGenerator.generate(count);
        log.info("已生成 {} 条日志数据", logEntries.size());
        return logEntries.size();
    }

    /**
     * 获取当前日志数量
     */
    public int getLogCount() {
        return logEntries.size();
    }

    /**
     * 执行 PV/UV 分析
     */
    public Map<String, PageStats> analyzePvUv() {
        if (logEntries.isEmpty()) {
            log.warn("没有日志数据，请先生成日志");
            return Collections.emptyMap();
        }

        log.info("开始执行 PV/UV 分析...");

        Map<String, PageStats> result = mapReduceEngine.execute(
                logEntries,
                pvUvMapper,
                pvUvReducer);

        log.info("PV/UV 分析完成，共 {} 个页面", result.size());
        return result;
    }

    /**
     * 获取热门页面 Top N
     */
    public List<Map.Entry<String, Long>> getTopNPages(int n) {
        if (logEntries.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始执行 Top{} 分析...", n);

        Map<String, Long> countMap = mapReduceEngine.execute(
                logEntries,
                topNMapper,
                topNReducer);

        // 按访问量排序并取 Top N
        List<Map.Entry<String, Long>> topN = countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .collect(Collectors.toList());

        log.info("Top{} 分析完成", n);
        return topN;
    }

    /**
     * 分析用户访问路径
     */
    public List<UserPath> analyzeUserPaths() {
        if (logEntries.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("开始分析用户访问路径...");

        // 按用户分组
        Map<String, List<LogEntry>> userLogs = logEntries.stream()
                .filter(e -> e.getUserId() != null)
                .collect(Collectors.groupingBy(LogEntry::getUserId));

        List<UserPath> paths = new ArrayList<>();

        for (Map.Entry<String, List<LogEntry>> entry : userLogs.entrySet()) {
            String userId = entry.getKey();
            List<LogEntry> logs = entry.getValue();

            // 按时间排序
            logs.sort(Comparator.comparing(LogEntry::getTimestamp));

            // 提取访问路径
            List<String> path = logs.stream()
                    .map(LogEntry::getPagePath)
                    .collect(Collectors.toList());

            UserPath userPath = UserPath.builder()
                    .userId(userId)
                    .path(path)
                    .visitCount(path.size())
                    .totalDuration(calculateDuration(logs))
                    .build();

            paths.add(userPath);
        }

        // 按访问次数排序
        paths.sort(Comparator.comparingInt(UserPath::getVisitCount).reversed());

        log.info("用户路径分析完成，共 {} 个用户", paths.size());
        return paths;
    }

    /**
     * 获取概览统计
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        overview.put("totalPV", logEntries.size());
        overview.put("totalUV", logEntries.stream()
                .map(LogEntry::getUserId)
                .distinct()
                .count());
        overview.put("avgResponseTime", logEntries.stream()
                .mapToLong(LogEntry::getResponseTime)
                .average()
                .orElse(0.0));
        overview.put("errorRate", calculateErrorRate());
        overview.put("totalPages", logEntries.stream()
                .map(LogEntry::getPagePath)
                .distinct()
                .count());

        return overview;
    }

    private long calculateDuration(List<LogEntry> logs) {
        if (logs.size() < 2)
            return 0;
        LogEntry first = logs.get(0);
        LogEntry last = logs.get(logs.size() - 1);
        return java.time.Duration.between(first.getTimestamp(), last.getTimestamp()).toSeconds();
    }

    private double calculateErrorRate() {
        if (logEntries.isEmpty())
            return 0;
        long errors = logEntries.stream()
                .filter(e -> e.getStatusCode() >= 400)
                .count();
        return (double) errors / logEntries.size() * 100;
    }
}
