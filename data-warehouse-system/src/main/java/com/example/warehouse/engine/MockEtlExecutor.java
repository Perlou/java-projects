package com.example.warehouse.engine;

import com.example.warehouse.model.EtlTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * ETL 执行器（模拟）
 * 
 * 模拟 ETL 任务的执行，包括：
 * - 任务调度
 * - 执行状态跟踪
 * - 依赖检查
 */
@Component
public class MockEtlExecutor {

    private static final Logger log = LoggerFactory.getLogger(MockEtlExecutor.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ConcurrentHashMap<Long, TaskExecution> runningTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("MockEtlExecutor 初始化完成");
        log.info("【数仓知识点】ETL 任务执行器");
        log.info("  模拟: 任务调度、状态跟踪、依赖检查");
    }

    /**
     * 执行 ETL 任务（异步模拟）
     */
    public TaskExecution execute(EtlTask task) {
        log.info("开始执行 ETL 任务: {} ({})", task.getTaskName(), task.getTaskType());

        TaskExecution execution = new TaskExecution();
        execution.setTaskId(task.getId());
        execution.setTaskName(task.getTaskName());
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(EtlTask.TaskStatus.RUNNING);

        runningTasks.put(task.getId(), execution);

        // 异步执行模拟
        CompletableFuture.runAsync(() -> {
            try {
                log.info("【ETL 执行】解析 SQL...");
                Thread.sleep(500); // 模拟解析

                log.info("【ETL 执行】读取源表: {}", task.getSourceTables());
                Thread.sleep(1000); // 模拟读取

                log.info("【ETL 执行】执行转换...");
                Thread.sleep(1500); // 模拟转换

                log.info("【ETL 执行】写入目标表: {}", task.getTargetTable());
                Thread.sleep(500); // 模拟写入

                // 模拟成功
                execution.setStatus(EtlTask.TaskStatus.SUCCESS);
                execution.setEndTime(LocalDateTime.now());
                execution.setRecordsProcessed(10000 + (long) (Math.random() * 90000));

                log.info("✓ ETL 任务完成: {}, 处理记录数: {}",
                        task.getTaskName(), execution.getRecordsProcessed());

            } catch (InterruptedException e) {
                execution.setStatus(EtlTask.TaskStatus.FAILED);
                execution.setErrorMessage("任务被中断");
                log.error("✗ ETL 任务中断: {}", task.getTaskName());
            } catch (Exception e) {
                execution.setStatus(EtlTask.TaskStatus.FAILED);
                execution.setErrorMessage(e.getMessage());
                log.error("✗ ETL 任务失败: {}", task.getTaskName(), e);
            } finally {
                execution.setEndTime(LocalDateTime.now());
            }
        }, executorService);

        return execution;
    }

    /**
     * 获取任务执行状态
     */
    public TaskExecution getExecution(Long taskId) {
        return runningTasks.get(taskId);
    }

    /**
     * 停止任务
     */
    public boolean stopTask(Long taskId) {
        TaskExecution execution = runningTasks.get(taskId);
        if (execution != null && execution.getStatus() == EtlTask.TaskStatus.RUNNING) {
            execution.setStatus(EtlTask.TaskStatus.STOPPED);
            execution.setEndTime(LocalDateTime.now());
            log.info("任务已停止: {}", taskId);
            return true;
        }
        return false;
    }

    /**
     * 任务执行信息
     */
    public static class TaskExecution {
        private Long taskId;
        private String taskName;
        private EtlTask.TaskStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long recordsProcessed;
        private String errorMessage;

        // Getters and Setters
        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public EtlTask.TaskStatus getStatus() {
            return status;
        }

        public void setStatus(EtlTask.TaskStatus status) {
            this.status = status;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Long getRecordsProcessed() {
            return recordsProcessed;
        }

        public void setRecordsProcessed(Long recordsProcessed) {
            this.recordsProcessed = recordsProcessed;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getDurationMs() {
            if (startTime == null)
                return 0;
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).toMillis();
        }
    }
}
