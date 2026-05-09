package com.example.loganalyzer.model;

/**
 * 页面统计结果
 */
public class PageStats {

    private String pagePath;
    private long pv;
    private long uv;
    private double avgResponseTime;
    private long totalResponseSize;
    private long errorCount;

    public PageStats() {
    }

    private PageStats(Builder builder) {
        this.pagePath = builder.pagePath;
        this.pv = builder.pv;
        this.uv = builder.uv;
        this.avgResponseTime = builder.avgResponseTime;
        this.totalResponseSize = builder.totalResponseSize;
        this.errorCount = builder.errorCount;
    }

    // Getters and Setters
    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(String pagePath) {
        this.pagePath = pagePath;
    }

    public long getPv() {
        return pv;
    }

    public void setPv(long pv) {
        this.pv = pv;
    }

    public long getUv() {
        return uv;
    }

    public void setUv(long uv) {
        this.uv = uv;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public long getTotalResponseSize() {
        return totalResponseSize;
    }

    public void setTotalResponseSize(long totalResponseSize) {
        this.totalResponseSize = totalResponseSize;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public double getErrorRate() {
        return pv > 0 ? (double) errorCount / pv * 100 : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pagePath;
        private long pv;
        private long uv;
        private double avgResponseTime;
        private long totalResponseSize;
        private long errorCount;

        public Builder pagePath(String pagePath) {
            this.pagePath = pagePath;
            return this;
        }

        public Builder pv(long pv) {
            this.pv = pv;
            return this;
        }

        public Builder uv(long uv) {
            this.uv = uv;
            return this;
        }

        public Builder avgResponseTime(double avgResponseTime) {
            this.avgResponseTime = avgResponseTime;
            return this;
        }

        public Builder totalResponseSize(long totalResponseSize) {
            this.totalResponseSize = totalResponseSize;
            return this;
        }

        public Builder errorCount(long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public PageStats build() {
            return new PageStats(this);
        }
    }
}
