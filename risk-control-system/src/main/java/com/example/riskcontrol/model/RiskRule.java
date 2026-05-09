package com.example.riskcontrol.model;

import java.time.LocalDateTime;

/**
 * 风控规则模型
 * 
 * 【Flink 概念对应】
 * 在真实 Flink 应用中，规则会通过 Broadcast State 广播给所有算子
 * 这里模拟了动态规则管理的概念
 */
public class RiskRule {

    private String ruleId; // 规则 ID
    private String ruleName; // 规则名称
    private String ruleType; // 规则类型: THRESHOLD, PATTERN, BLACKLIST
    private String targetField; // 目标字段
    private String operator; // 操作符: GT, LT, EQ, IN, BETWEEN
    private String threshold; // 阈值
    private int priority; // 优先级 (1-10, 10最高)
    private String action; // 触发动作: BLOCK, ALERT, REVIEW
    private int riskScore; // 风险分数增量
    private boolean enabled; // 是否启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String description; // 规则描述

    public RiskRule() {
        this.enabled = true;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public RiskRule(String ruleId, String ruleName, String ruleType,
            String targetField, String operator, String threshold,
            int priority, String action, int riskScore) {
        this();
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.targetField = targetField;
        this.operator = operator;
        this.threshold = threshold;
        this.priority = priority;
        this.action = action;
        this.riskScore = riskScore;
    }

    // Getters and Setters
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "RiskRule{ruleId='" + ruleId + "', ruleName='" + ruleName +
                "', action='" + action + "', enabled=" + enabled + "}";
    }
}
