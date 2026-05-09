package com.example.loganalyzer.model;

import java.time.LocalDateTime;

/**
 * 日志条目实体
 * 
 * 模拟 Web 服务器日志格式（类似 Apache/Nginx Combined Log Format）
 */
public class LogEntry {

    private String ip;
    private String userId;
    private LocalDateTime timestamp;
    private String method;
    private String url;
    private int statusCode;
    private long responseSize;
    private long responseTime;
    private String userAgent;
    private String referer;

    public LogEntry() {
    }

    private LogEntry(Builder builder) {
        this.ip = builder.ip;
        this.userId = builder.userId;
        this.timestamp = builder.timestamp;
        this.method = builder.method;
        this.url = builder.url;
        this.statusCode = builder.statusCode;
        this.responseSize = builder.responseSize;
        this.responseTime = builder.responseTime;
        this.userAgent = builder.userAgent;
        this.referer = builder.referer;
    }

    // Getters and Setters
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(long responseSize) {
        this.responseSize = responseSize;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getPagePath() {
        if (url == null)
            return "/";
        int idx = url.indexOf('?');
        return idx > 0 ? url.substring(0, idx) : url;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 400;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ip;
        private String userId;
        private LocalDateTime timestamp;
        private String method;
        private String url;
        private int statusCode;
        private long responseSize;
        private long responseTime;
        private String userAgent;
        private String referer;

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder responseSize(long responseSize) {
            this.responseSize = responseSize;
            return this;
        }

        public Builder responseTime(long responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(this);
        }
    }
}
