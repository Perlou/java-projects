package com.example.warehouse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * 列元数据
 */
@Entity
@Table(name = "column_metadata")
public class ColumnMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    @JsonIgnore
    private TableMetadata table;

    @Column(name = "table_id", insertable = false, updatable = false)
    private Long tableId;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "column_type", nullable = false)
    private String columnType;

    @Column(name = "column_comment")
    private String columnComment;

    @Column(name = "is_partition")
    private Boolean isPartition = false;

    @Column(name = "is_primary_key")
    private Boolean isPrimaryKey = false;

    @Column(name = "ordinal_position")
    private Integer ordinalPosition;

    // Constructors
    public ColumnMetadata() {
    }

    public ColumnMetadata(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TableMetadata getTable() {
        return table;
    }

    public void setTable(TableMetadata table) {
        this.table = table;
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

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public Boolean getIsPartition() {
        return isPartition;
    }

    public void setIsPartition(Boolean partition) {
        isPartition = partition;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }
}
