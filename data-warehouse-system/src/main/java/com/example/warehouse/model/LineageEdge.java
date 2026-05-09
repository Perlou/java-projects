package com.example.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据血缘边
 * 
 * 表示源表/列到目标表/列的数据流转关系
 */
@Entity
@Table(name = "lineage_edge")
public class LineageEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_table_id", nullable = false)
    private Long sourceTableId;

    @Column(name = "target_table_id", nullable = false)
    private Long targetTableId;

    @Column(name = "source_column")
    private String sourceColumn;

    @Column(name = "target_column")
    private String targetColumn;

    @Column(name = "transformation")
    private String transformation;

    @Column(name = "etl_task_id")
    private Long etlTaskId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 非持久化字段，用于展示
    @Transient
    private String sourceTableName;

    @Transient
    private String targetTableName;

    @Transient
    private String sourceTableLayer;

    @Transient
    private String targetTableLayer;

    // Constructors
    public LineageEdge() {
        this.createTime = LocalDateTime.now();
    }

    public LineageEdge(Long sourceTableId, Long targetTableId) {
        this();
        this.sourceTableId = sourceTableId;
        this.targetTableId = targetTableId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceTableId() {
        return sourceTableId;
    }

    public void setSourceTableId(Long sourceTableId) {
        this.sourceTableId = sourceTableId;
    }

    public Long getTargetTableId() {
        return targetTableId;
    }

    public void setTargetTableId(Long targetTableId) {
        this.targetTableId = targetTableId;
    }

    public String getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public Long getEtlTaskId() {
        return etlTaskId;
    }

    public void setEtlTaskId(Long etlTaskId) {
        this.etlTaskId = etlTaskId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }

    public String getSourceTableLayer() {
        return sourceTableLayer;
    }

    public void setSourceTableLayer(String sourceTableLayer) {
        this.sourceTableLayer = sourceTableLayer;
    }

    public String getTargetTableLayer() {
        return targetTableLayer;
    }

    public void setTargetTableLayer(String targetTableLayer) {
        this.targetTableLayer = targetTableLayer;
    }
}
