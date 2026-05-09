package com.example.warehouse.service;

import com.example.warehouse.engine.MockEtlExecutor;
import com.example.warehouse.model.EtlTask;
import com.example.warehouse.repository.EtlTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ETL 服务
 */
@Service
public class EtlService {

    private static final Logger log = LoggerFactory.getLogger(EtlService.class);

    @Autowired
    private EtlTaskRepository taskRepository;

    @Autowired
    private MockEtlExecutor etlExecutor;

    @PostConstruct
    public void init() {
        log.info("EtlService 初始化完成");
        log.info("【数仓知识点】ETL 任务管理:");
        log.info("  • 任务类型: SPARK, HIVE, FLINK");
        log.info("  • 任务调度: Cron 表达式");
        log.info("  • 依赖管理: 源表 → 目标表");
    }

    /**
     * 获取所有任务
     */
    public List<EtlTask> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * 获取任务详情
     */
    public Optional<EtlTask> getTask(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * 创建任务
     */
    public EtlTask createTask(EtlTask task) {
        return taskRepository.save(task);
    }

    /**
     * 执行任务
     */
    public MockEtlExecutor.TaskExecution executeTask(Long taskId) {
        Optional<EtlTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        EtlTask task = taskOpt.get();

        // 执行任务
        MockEtlExecutor.TaskExecution execution = etlExecutor.execute(task);

        // 更新任务状态
        task.setStatus(EtlTask.TaskStatus.RUNNING);
        task.setLastRunTime(LocalDateTime.now());
        taskRepository.save(task);

        return execution;
    }

    /**
     * 获取任务执行状态
     */
    public MockEtlExecutor.TaskExecution getExecutionStatus(Long taskId) {
        return etlExecutor.getExecution(taskId);
    }

    /**
     * 停止任务
     */
    public boolean stopTask(Long taskId) {
        boolean stopped = etlExecutor.stopTask(taskId);
        if (stopped) {
            taskRepository.findById(taskId).ifPresent(task -> {
                task.setStatus(EtlTask.TaskStatus.STOPPED);
                taskRepository.save(task);
            });
        }
        return stopped;
    }

    /**
     * 获取任务统计
     */
    public Map<String, Object> getTaskStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalTasks", taskRepository.count());

        // 按状态统计
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (EtlTask.TaskStatus status : EtlTask.TaskStatus.values()) {
            byStatus.put(status.name(), (long) taskRepository.findByStatus(status).size());
        }
        stats.put("byStatus", byStatus);

        return stats;
    }

    /**
     * 获取任务依赖图
     */
    public Map<String, Object> getTaskDependencies() {
        Map<String, Object> deps = new LinkedHashMap<>();

        List<Map<String, Object>> tasks = new ArrayList<>();
        for (EtlTask task : taskRepository.findAll()) {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("id", task.getId());
            t.put("name", task.getTaskName());
            t.put("type", task.getTaskType());
            t.put("sources", task.getSourceTableArray());
            t.put("target", task.getTargetTable());
            t.put("status", task.getStatus().name());
            tasks.add(t);
        }
        deps.put("tasks", tasks);

        return deps;
    }
}
