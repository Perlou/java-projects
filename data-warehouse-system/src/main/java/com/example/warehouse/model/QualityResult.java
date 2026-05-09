package com.example.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据质量检测结果
 */
@Entity
@Table(name = "quality_result")
public class QualityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "check_time")
    private LocalDateTime checkTime;

    @Column(name = "total_count")
    private Long totalCount;

    @Column(name = "pass_count")
    private Long passCount;

    @Column(name = "fail_count")
    private Long failCount;

    @Column(name = "pass_rate")
    private Double passRate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CheckStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Transient
    private String ruleName;

    @Transient
    private String tableName;

    /**
     * 检测状态枚举
     */
    public enum CheckStatus {
        PASS("通过"),
        FAIL("未通过"),
        ERROR("检测异常");

        private final String displayName;

        CheckStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public QualityResult() {
        this.checkTime = LocalDateTime.now();
    }

    public QualityResult(Long ruleId, Long tableId) {
        this();
        this.ruleId = ruleId;
        this.tableId = tableId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getPassCount() {
        return passCount;
    }

    public void setPassCount(Long passCount) {
        this.passCount = passCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public void setStatus(CheckStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
