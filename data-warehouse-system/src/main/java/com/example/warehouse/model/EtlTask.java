package com.example.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ETL 任务
 */
@Entity
@Table(name = "etl_task")
public class EtlTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_type")
    private String taskType; // SPARK, HIVE, FLINK

    @Column(name = "source_tables")
    private String sourceTables; // 逗号分隔

    @Column(name = "target_table")
    private String targetTable;

    @Column(name = "sql_content", columnDefinition = "TEXT")
    private String sqlContent;

    @Column(name = "schedule_cron")
    private String scheduleCron;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.STOPPED;

    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        STOPPED("已停止"),
        RUNNING("运行中"),
        SUCCESS("执行成功"),
        FAILED("执行失败");

        private final String displayName;

        TaskStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public EtlTask() {
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getSourceTables() {
        return sourceTables;
    }

    public void setSourceTables(String sourceTables) {
        this.sourceTables = sourceTables;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getSqlContent() {
        return sqlContent;
    }

    public void setSqlContent(String sqlContent) {
        this.sqlContent = sqlContent;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取源表列表
     */
    public String[] getSourceTableArray() {
        if (sourceTables == null || sourceTables.isEmpty()) {
            return new String[0];
        }
        return sourceTables.split(",");
    }
}
