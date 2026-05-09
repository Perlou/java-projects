package com.example.riskcontrol.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户风险画像
 * 
 * 【Flink 概念对应】
 * 在 Flink 中，这相当于 Keyed State
 * 每个用户（Key）有独立的状态，存储其风险相关信息
 */
public class UserRiskProfile {

    private String userId;
    private int riskScore; // 当前风险分数 (0-100)
    private String riskLevel; // 风险等级: LOW, MEDIUM, HIGH, CRITICAL

    // 交易统计（模拟窗口聚合结果）
    private int transactionCount1h; // 1小时内交易次数
    private int transactionCount24h; // 24小时内交易次数
    private BigDecimal totalAmount1h; // 1小时内交易金额
    private BigDecimal totalAmount24h; // 24小时内交易金额

    // 行为特征
    private Set<String> recentCities; // 近期交易城市
    private Set<String> recentDevices; // 近期使用设备
    private int loginFailCount; // 连续登录失败次数
    private int alertCount; // 告警次数

    // 时间相关
    private LocalDateTime lastTransactionTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastAlertTime;
    private LocalDateTime profileUpdateTime;

    public UserRiskProfile() {
        this.riskScore = 50; // 初始风险分数
        this.riskLevel = "MEDIUM";
        this.totalAmount1h = BigDecimal.ZERO;
        this.totalAmount24h = BigDecimal.ZERO;
        this.recentCities = new HashSet<>();
        this.recentDevices = new HashSet<>();
        this.profileUpdateTime = LocalDateTime.now();
    }

    public UserRiskProfile(String userId) {
        this();
        this.userId = userId;
    }

    /**
     * 更新风险等级
     */
    public void updateRiskLevel() {
        if (riskScore >= 80) {
            this.riskLevel = "CRITICAL";
        } else if (riskScore >= 60) {
            this.riskLevel = "HIGH";
        } else if (riskScore >= 40) {
            this.riskLevel = "MEDIUM";
        } else {
            this.riskLevel = "LOW";
        }
    }

    /**
     * 增加风险分数
     */
    public void addRiskScore(int delta) {
        this.riskScore = Math.min(100, Math.max(0, this.riskScore + delta));
        updateRiskLevel();
        this.profileUpdateTime = LocalDateTime.now();
    }

    /**
     * 记录交易
     */
    public void recordTransaction(Transaction tx) {
        this.transactionCount1h++;
        this.transactionCount24h++;
        this.totalAmount1h = this.totalAmount1h.add(tx.getAmount());
        this.totalAmount24h = this.totalAmount24h.add(tx.getAmount());
        this.recentCities.add(tx.getCity());
        if (tx.getDeviceId() != null) {
            this.recentDevices.add(tx.getDeviceId());
        }
        this.lastTransactionTime = tx.getEventTime();
        this.profileUpdateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
        updateRiskLevel();
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getTransactionCount1h() {
        return transactionCount1h;
    }

    public void setTransactionCount1h(int transactionCount1h) {
        this.transactionCount1h = transactionCount1h;
    }

    public int getTransactionCount24h() {
        return transactionCount24h;
    }

    public void setTransactionCount24h(int transactionCount24h) {
        this.transactionCount24h = transactionCount24h;
    }

    public BigDecimal getTotalAmount1h() {
        return totalAmount1h;
    }

    public void setTotalAmount1h(BigDecimal totalAmount1h) {
        this.totalAmount1h = totalAmount1h;
    }

    public BigDecimal getTotalAmount24h() {
        return totalAmount24h;
    }

    public void setTotalAmount24h(BigDecimal totalAmount24h) {
        this.totalAmount24h = totalAmount24h;
    }

    public Set<String> getRecentCities() {
        return recentCities;
    }

    public void setRecentCities(Set<String> recentCities) {
        this.recentCities = recentCities;
    }

    public Set<String> getRecentDevices() {
        return recentDevices;
    }

    public void setRecentDevices(Set<String> recentDevices) {
        this.recentDevices = recentDevices;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(int loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
    }

    public LocalDateTime getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(LocalDateTime lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getLastAlertTime() {
        return lastAlertTime;
    }

    public void setLastAlertTime(LocalDateTime lastAlertTime) {
        this.lastAlertTime = lastAlertTime;
    }

    public LocalDateTime getProfileUpdateTime() {
        return profileUpdateTime;
    }

    public void setProfileUpdateTime(LocalDateTime profileUpdateTime) {
        this.profileUpdateTime = profileUpdateTime;
    }

    @Override
    public String toString() {
        return "UserRiskProfile{userId='" + userId + "', riskScore=" + riskScore +
                ", riskLevel='" + riskLevel + "', txCount1h=" + transactionCount1h + "}";
    }
}
