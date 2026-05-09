package com.example.analytics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分析报告模型
 * 
 * 包含完整的数据分析报告，整合多个指标结果
 * 
 * @author Java Course
 * 
 */
public class AnalyticsReport implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报告 ID */
    private String reportId;

    /** 报告名称 */
    private String reportName;

    /** 报告类型（实时/批量） */
    private ReportType reportType;

    /** 统计时间范围 - 开始 */
    private LocalDateTime startTime;

    /** 统计时间范围 - 结束 */
    private LocalDateTime endTime;

    /** 报告生成时间 */
    private LocalDateTime generatedAt;

    /** 核心指标 */
    private CoreMetrics coreMetrics;

    /** 详细指标列表 */
    private List<MetricResult> metricResults;

    /** 维度分析数据 */
    private Map<String, List<MetricResult>> dimensionAnalysis;

    /** 处理信息 */
    private ProcessingInfo processingInfo;

    /**
     * 报告类型枚举
     */
    public enum ReportType {
        REALTIME, // 实时报告
        HOURLY, // 小时报告
        DAILY, // 日报
        WEEKLY, // 周报
        MONTHLY // 月报
    }

    /**
     * 核心指标
     */
    public static class CoreMetrics implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 页面浏览量 */
        private Long pv;

        /** 独立访客数 */
        private Long uv;

        /** 会话数 */
        private Long sessions;

        /** 转化次数 */
        private Long conversions;

        /** 转化率 */
        private Double conversionRate;

        /** 平均停留时长（秒） */
        private Double avgDuration;

        /** 跳出率 */
        private Double bounceRate;

        // Getters and Setters
        public Long getPv() {
            return pv;
        }

        public void setPv(Long pv) {
            this.pv = pv;
        }

        public Long getUv() {
            return uv;
        }

        public void setUv(Long uv) {
            this.uv = uv;
        }

        public Long getSessions() {
            return sessions;
        }

        public void setSessions(Long sessions) {
            this.sessions = sessions;
        }

        public Long getConversions() {
            return conversions;
        }

        public void setConversions(Long conversions) {
            this.conversions = conversions;
        }

        public Double getConversionRate() {
            return conversionRate;
        }

        public void setConversionRate(Double conversionRate) {
            this.conversionRate = conversionRate;
        }

        public Double getAvgDuration() {
            return avgDuration;
        }

        public void setAvgDuration(Double avgDuration) {
            this.avgDuration = avgDuration;
        }

        public Double getBounceRate() {
            return bounceRate;
        }

        public void setBounceRate(Double bounceRate) {
            this.bounceRate = bounceRate;
        }
    }

    /**
     * 处理信息
     */
    public static class ProcessingInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 处理的数据量 */
        private Long recordCount;

        /** 处理耗时（毫秒） */
        private Long processingTimeMs;

        /** Spark 作业数 */
        private Integer sparkJobs;

        /** Stage 数 */
        private Integer stages;

        /** Task 数 */
        private Integer tasks;

        // Getters and Setters
        public Long getRecordCount() {
            return recordCount;
        }

        public void setRecordCount(Long recordCount) {
            this.recordCount = recordCount;
        }

        public Long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public void setProcessingTimeMs(Long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }

        public Integer getSparkJobs() {
            return sparkJobs;
        }

        public void setSparkJobs(Integer sparkJobs) {
            this.sparkJobs = sparkJobs;
        }

        public Integer getStages() {
            return stages;
        }

        public void setStages(Integer stages) {
            this.stages = stages;
        }

        public Integer getTasks() {
            return tasks;
        }

        public void setTasks(Integer tasks) {
            this.tasks = tasks;
        }
    }

    public AnalyticsReport() {
        this.generatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
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

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public CoreMetrics getCoreMetrics() {
        return coreMetrics;
    }

    public void setCoreMetrics(CoreMetrics coreMetrics) {
        this.coreMetrics = coreMetrics;
    }

    public List<MetricResult> getMetricResults() {
        return metricResults;
    }

    public void setMetricResults(List<MetricResult> metricResults) {
        this.metricResults = metricResults;
    }

    public Map<String, List<MetricResult>> getDimensionAnalysis() {
        return dimensionAnalysis;
    }

    public void setDimensionAnalysis(Map<String, List<MetricResult>> dimensionAnalysis) {
        this.dimensionAnalysis = dimensionAnalysis;
    }

    public ProcessingInfo getProcessingInfo() {
        return processingInfo;
    }

    public void setProcessingInfo(ProcessingInfo processingInfo) {
        this.processingInfo = processingInfo;
    }
}
