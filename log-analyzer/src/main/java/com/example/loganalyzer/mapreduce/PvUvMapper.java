package com.example.loganalyzer.mapreduce;

import com.example.loganalyzer.model.LogEntry;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * PV/UV 统计 Mapper
 * 
 * 输入：日志条目
 * 输出：<页面路径, 日志条目>
 */
@Component
public class PvUvMapper implements Mapper<Integer, LogEntry, String, LogEntry> {

    @Override
    public List<Map.Entry<String, LogEntry>> map(Integer key, LogEntry value) {
        if (value == null || value.getUrl() == null) {
            return Collections.emptyList();
        }

        // 只统计成功的页面访问
        if (value.isSuccess()) {
            String pagePath = value.getPagePath();
            return Collections.singletonList(
                    new AbstractMap.SimpleEntry<>(pagePath, value));
        }

        return Collections.emptyList();
    }
}
