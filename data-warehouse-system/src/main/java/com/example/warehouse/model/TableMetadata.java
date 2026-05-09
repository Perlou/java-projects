package com.example.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 表元数据
 */
@Entity
@Table(name = "table_metadata")
public class TableMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "table_comment")
    private String tableComment;

    @Column(name = "dw_layer", nullable = false)
    @Enumerated(EnumType.STRING)
    private DwLayer dwLayer;

    @Column(name = "database_name")
    private String databaseName;

    @Column(name = "owner")
    private String owner;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColumnMetadata> columns = new ArrayList<>();

    // Constructors
    public TableMetadata() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public TableMetadata(String tableName, DwLayer dwLayer) {
        this();
        this.tableName = tableName;
        this.dwLayer = dwLayer;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public DwLayer getDwLayer() {
        return dwLayer;
    }

    public void setDwLayer(DwLayer dwLayer) {
        this.dwLayer = dwLayer;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
    }

    /**
     * 获取完整表名 (database.table)
     */
    public String getFullTableName() {
        if (databaseName != null && !databaseName.isEmpty()) {
            return databaseName + "." + tableName;
        }
        return tableName;
    }
}
