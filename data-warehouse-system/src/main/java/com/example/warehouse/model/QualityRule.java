package com.example.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据质量规则
 */
@Entity
@Table(name = "quality_rule")
public class QualityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "rule_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RuleType ruleType;

    @Column(name = "table_id")
    private Long tableId;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "rule_expression")
    private String ruleExpression;

    @Column(name = "threshold")
    private Double threshold = 100.0;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        /** 完整性 - 非空检查 */
        COMPLETENESS("完整性", "检查数据是否缺失"),

        /** 唯一性 - 重复检查 */
        UNIQUENESS("唯一性", "检查数据是否重复"),

        /** 有效性 - 格式/范围检查 */
        VALIDITY("有效性", "检查数据是否符合业务规则"),

        /** 一致性 - 跨表检查 */
        CONSISTENCY("一致性", "检查多源数据是否一致");

        private final String displayName;
        private final String description;

        RuleType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public QualityRule() {
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
