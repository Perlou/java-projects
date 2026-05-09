package com.example.loganalyzer.mapreduce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MapReduce 执行引擎
 * 
 * 模拟 Hadoop MapReduce 的执行流程：
 * 1. Map 阶段：并行处理输入数据，生成中间 key-value 对
 * 2. Shuffle 阶段：按 key 分组
 * 3. Reduce 阶段：对每个 key 的 values 进行聚合
 */
@Component
public class MapReduceEngine {

    private static final Logger log = LoggerFactory.getLogger(MapReduceEngine.class);

    /**
     * 执行 MapReduce 作业
     */
    public <T, K2, V2, K3, V3> Map<K3, V3> execute(
            List<T> input,
            Mapper<Integer, T, K2, V2> mapper,
            Reducer<K2, V2, K3, V3> reducer) {

        log.info("=== MapReduce 作业开始 ===");
        log.info("输入记录数: {}", input.size());

        // 1. Map 阶段
        log.info(">>> Map 阶段");
        List<Map.Entry<K2, V2>> mapOutput = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            List<Map.Entry<K2, V2>> result = mapper.map(i, input.get(i));
            if (result != null) {
                mapOutput.addAll(result);
            }
        }
        log.info("Map 输出: {} 条记录", mapOutput.size());

        // 2. Shuffle 阶段（按 key 分组）
        log.info(">>> Shuffle 阶段");
        Map<K2, List<V2>> shuffled = mapOutput.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        log.info("Shuffle 后分组数: {}", shuffled.size());

        // 3. Reduce 阶段
        log.info(">>> Reduce 阶段");
        Map<K3, V3> result = new LinkedHashMap<>();

        for (Map.Entry<K2, List<V2>> entry : shuffled.entrySet()) {
            Map.Entry<K3, V3> reduced = reducer.reduce(entry.getKey(), entry.getValue());
            if (reduced != null) {
                result.put(reduced.getKey(), reduced.getValue());
            }
        }
        log.info("Reduce 输出: {} 条记录", result.size());
        log.info("=== MapReduce 作业完成 ===");

        return result;
    }
}
