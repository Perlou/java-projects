package com.example.loganalyzer.controller;

import com.example.loganalyzer.model.*;
import com.example.loganalyzer.service.AnalyzerService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API 控制器
 * 
 * 提供日志分析的 REST 接口
 */
@RestController
@RequestMapping("/api")
public class AnalyzerController {

    private final AnalyzerService analyzerService;

    public AnalyzerController(AnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    /**
     * 生成模拟日志数据
     */
    @PostMapping("/logs/generate")
    public Map<String, Object> generateLogs(@RequestParam(defaultValue = "10000") int count) {
        int generated = analyzerService.generateLogs(count);
        return Map.of(
                "success", true,
                "message", "日志生成成功",
                "count", generated);
    }

    /**
     * 获取日志数量
     */
    @GetMapping("/logs/count")
    public Map<String, Object> getLogCount() {
        return Map.of("count", analyzerService.getLogCount());
    }

    /**
     * 获取概览统计
     */
    @GetMapping("/stats/overview")
    public Map<String, Object> getOverview() {
        return analyzerService.getOverview();
    }

    /**
     * 执行 PV/UV 分析
     */
    @GetMapping("/stats/pvuv")
    public List<PageStats> getPvUvStats() {
        Map<String, PageStats> result = analyzerService.analyzePvUv();
        List<PageStats> list = new ArrayList<>(result.values());
        // 按 PV 降序排序
        list.sort(Comparator.comparingLong(PageStats::getPv).reversed());
        return list;
    }

    /**
     * 获取热门页面 Top N
     */
    @GetMapping("/stats/topn")
    public List<Map<String, Object>> getTopNPages(
            @RequestParam(defaultValue = "10") int n) {
        List<Map.Entry<String, Long>> topN = analyzerService.getTopNPages(n);

        List<Map<String, Object>> result = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Long> entry : topN) {
            result.add(Map.of(
                    "rank", rank++,
                    "page", entry.getKey(),
                    "visits", entry.getValue()));
        }
        return result;
    }

    /**
     * 分析用户访问路径
     */
    @GetMapping("/stats/paths")
    public List<UserPath> getUserPaths(
            @RequestParam(defaultValue = "20") int limit) {
        List<UserPath> paths = analyzerService.analyzeUserPaths();
        return paths.stream().limit(limit).toList();
    }
}
