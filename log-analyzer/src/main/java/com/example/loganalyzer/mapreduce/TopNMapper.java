package com.example.loganalyzer.mapreduce;

import com.example.loganalyzer.model.LogEntry;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TopN Mapper
 * 
 * 输入：日志条目
 * 输出：<页面路径, 1>
 * 
 * 用于统计每个页面的访问次数
 */
@Component
public class TopNMapper implements Mapper<Integer, LogEntry, String, Integer> {

    @Override
    public List<Map.Entry<String, Integer>> map(Integer key, LogEntry value) {
        if (value == null || value.getUrl() == null) {
            return Collections.emptyList();
        }

        // 统计所有页面访问（包括错误页面）
        String pagePath = value.getPagePath();
        return Collections.singletonList(
                new AbstractMap.SimpleEntry<>(pagePath, 1));
    }
}
