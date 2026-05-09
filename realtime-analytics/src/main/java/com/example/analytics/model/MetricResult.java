package com.example.analytics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指标结果模型
 * 
 * 封装各类分析指标的计算结果
 * 
 * @author Java Course
 * 
 */
public class MetricResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 指标名称 */
    private String metricName;

    /** 指标值 */
    private Object value;

    /** 统计维度（如：按小时、按页面、按用户等） */
    private String dimension;

    /** 维度值 */
    private String dimensionValue;

    /** 统计时间范围 - 开始 */
    private LocalDateTime startTime;

    /** 统计时间范围 - 结束 */
    private LocalDateTime endTime;

    /** 计算时间 */
    private LocalDateTime calculateTime;

    /** 额外数据 */
    private Map<String, Object> extra;

    public MetricResult() {
        this.calculateTime = LocalDateTime.now();
    }

    public MetricResult(String metricName, Object value) {
        this();
        this.metricName = metricName;
        this.value = value;
    }

    // Static factory methods
    public static MetricResult of(String name, Object value) {
        return new MetricResult(name, value);
    }

    public static MetricResult of(String name, Object value, String dimension, String dimensionValue) {
        MetricResult result = new MetricResult(name, value);
        result.setDimension(dimension);
        result.setDimensionValue(dimensionValue);
        return result;
    }

    // Getters and Setters
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(String dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getCalculateTime() {
        return calculateTime;
    }

    public void setCalculateTime(LocalDateTime calculateTime) {
        this.calculateTime = calculateTime;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "MetricResult{" +
                "metricName='" + metricName + '\'' +
                ", value=" + value +
                ", dimension='" + dimension + '\'' +
                ", dimensionValue='" + dimensionValue + '\'' +
                ", calculateTime=" + calculateTime +
                '}';
    }
}
