package com.example.loganalyzer.mapreduce;

import java.util.List;

/**
 * Reducer 接口
 * 
 * 模拟 Hadoop MapReduce 的 Reducer
 * 
 * @param <K2> 输入 Key 类型（来自 Mapper 输出）
 * @param <V2> 输入 Value 类型（来自 Mapper 输出）
 * @param <K3> 输出 Key 类型
 * @param <V3> 输出 Value 类型
 */
@FunctionalInterface
public interface Reducer<K2, V2, K3, V3> {

    /**
     * Reduce 操作
     * 
     * @param key    输入键（分组后的键）
     * @param values 该键对应的所有值
     * @return 输出的 key-value 对
     */
    java.util.Map.Entry<K3, V3> reduce(K2 key, List<V2> values);
}
