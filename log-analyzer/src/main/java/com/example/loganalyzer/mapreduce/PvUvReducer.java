package com.example.loganalyzer.mapreduce;

import com.example.loganalyzer.model.LogEntry;
import com.example.loganalyzer.model.PageStats;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PV/UV 统计 Reducer
 * 
 * 输入：<页面路径, 日志条目列表>
 * 输出：<页面路径, 页面统计>
 */
@Component
public class PvUvReducer implements Reducer<String, LogEntry, String, PageStats> {

        @Override
        public Map.Entry<String, PageStats> reduce(String key, List<LogEntry> values) {
                if (values == null || values.isEmpty()) {
                        return null;
                }

                // 计算 PV
                long pv = values.size();

                // 计算 UV（独立访客）
                long uv = values.stream()
                                .map(LogEntry::getUserId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .count();

                // 计算平均响应时间
                double avgResponseTime = values.stream()
                                .mapToLong(LogEntry::getResponseTime)
                                .average()
                                .orElse(0.0);

                // 计算总响应大小
                long totalResponseSize = values.stream()
                                .mapToLong(LogEntry::getResponseSize)
                                .sum();

                // 计算错误数
                long errorCount = values.stream()
                                .filter(e -> e.getStatusCode() >= 400)
                                .count();

                PageStats stats = PageStats.builder()
                                .pagePath(key)
                                .pv(pv)
                                .uv(uv)
                                .avgResponseTime(avgResponseTime)
                                .totalResponseSize(totalResponseSize)
                                .errorCount(errorCount)
                                .build();

                return new AbstractMap.SimpleEntry<>(key, stats);
        }
}
