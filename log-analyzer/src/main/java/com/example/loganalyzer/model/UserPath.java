package com.example.loganalyzer.model;

import java.util.List;

/**
 * 用户访问路径
 */
public class UserPath {

    private String userId;
    private List<String> path;
    private int visitCount;
    private long totalDuration;

    public UserPath() {
    }

    private UserPath(Builder builder) {
        this.userId = builder.userId;
        this.path = builder.path;
        this.visitCount = builder.visitCount;
        this.totalDuration = builder.totalDuration;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public String getPathString() {
        return path != null ? String.join(" -> ", path) : "";
    }

    public int getPathLength() {
        return path != null ? path.size() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private List<String> path;
        private int visitCount;
        private long totalDuration;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder path(List<String> path) {
            this.path = path;
            return this;
        }

        public Builder visitCount(int visitCount) {
            this.visitCount = visitCount;
            return this;
        }

        public Builder totalDuration(long totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }

        public UserPath build() {
            return new UserPath(this);
        }
    }
}
