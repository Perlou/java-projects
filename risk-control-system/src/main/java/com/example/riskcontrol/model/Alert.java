package com.example.riskcontrol.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 告警模型
 * 
 * 【Flink 概念对应】
 * 在 Flink CEP 中，当模式匹配成功时会触发告警
 * 这里模拟了 PatternProcessFunction 的输出
 */
public class Alert {

    public enum AlertLevel {
        LOW, // 低风险
        MEDIUM, // 中风险
        HIGH, // 高风险
        CRITICAL // 严重风险
    }

    public enum AlertStatus {
        PENDING, // 待处理
        CONFIRMED, // 已确认
        DISMISSED, // 已忽略
        RESOLVED // 已解决
    }

    private String alertId;
    private String userId;
    private String alertType; // 告警类型: LOGIN_FAILURE, CROSS_CITY, HIGH_AMOUNT
    private AlertLevel level;
    private AlertStatus status;
    private String message;
    private String ruleId; // 触发的规则ID
    private String patternName; // 匹配的模式名称
    private int riskScore;
    private LocalDateTime triggerTime;
    private LocalDateTime windowStart; // 窗口开始时间
    private LocalDateTime windowEnd; // 窗口结束时间
    private List<String> matchedEventIds; // 匹配的事件ID列表
    private String details; // 详细信息（JSON格式）

    public Alert() {
        this.status = AlertStatus.PENDING;
        this.triggerTime = LocalDateTime.now();
        this.matchedEventIds = new ArrayList<>();
    }

    public Alert(String alertId, String userId, String alertType,
            AlertLevel level, String message) {
        this();
        this.alertId = alertId;
        this.userId = userId;
        this.alertType = alertType;
        this.level = level;
        this.message = message;
    }

    // Getters and Setters
    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public AlertLevel getLevel() {
        return level;
    }

    public void setLevel(AlertLevel level) {
        this.level = level;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(LocalDateTime triggerTime) {
        this.triggerTime = triggerTime;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(LocalDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }

    public List<String> getMatchedEventIds() {
        return matchedEventIds;
    }

    public void setMatchedEventIds(List<String> matchedEventIds) {
        this.matchedEventIds = matchedEventIds;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "Alert{alertId='" + alertId + "', userId='" + userId +
                "', alertType='" + alertType + "', level=" + level +
                ", message='" + message + "'}";
    }
}
