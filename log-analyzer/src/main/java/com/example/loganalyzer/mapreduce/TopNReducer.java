package com.example.loganalyzer.mapreduce;

import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * TopN Reducer
 * 
 * 输入：<页面路径, 计数列表>
 * 输出：<页面路径, 总访问次数>
 */
@Component
public class TopNReducer implements Reducer<String, Integer, String, Long> {

    @Override
    public Map.Entry<String, Long> reduce(String key, List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        // 求和
        long total = values.stream()
                .mapToLong(Integer::longValue)
                .sum();

        return new AbstractMap.SimpleEntry<>(key, total);
    }
}
