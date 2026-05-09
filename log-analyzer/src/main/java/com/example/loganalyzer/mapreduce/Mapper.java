package com.example.loganalyzer.mapreduce;

import java.util.List;
import java.util.Map;

/**
 * Mapper 接口
 * 
 * 模拟 Hadoop MapReduce 的 Mapper
 * 
 * @param <K1> 输入 Key 类型
 * @param <V1> 输入 Value 类型
 * @param <K2> 输出 Key 类型
 * @param <V2> 输出 Value 类型
 */
@FunctionalInterface
public interface Mapper<K1, V1, K2, V2> {

    /**
     * Map 操作
     * 
     * @param key   输入键
     * @param value 输入值
     * @return 输出的 key-value 对列表
     */
    List<Map.Entry<K2, V2>> map(K1 key, V1 value);
}
