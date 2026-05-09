package com.example.warehouse.controller;

import com.example.warehouse.engine.MockEtlExecutor;
import com.example.warehouse.model.EtlTask;
import com.example.warehouse.service.EtlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ETL 任务控制器
 */
@RestController
@RequestMapping("/api/etl")
@Tag(name = "ETL 任务", description = "ETL 任务管理 API")
public class EtlController {

    @Autowired
    private EtlService etlService;

    @GetMapping("/tasks")
    @Operation(summary = "获取所有任务")
    public ResponseEntity<List<EtlTask>> getAllTasks() {
        return ResponseEntity.ok(etlService.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "获取任务详情")
    public ResponseEntity<EtlTask> getTask(@PathVariable Long id) {
        return etlService.getTask(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tasks")
    @Operation(summary = "创建任务")
    public ResponseEntity<EtlTask> createTask(@RequestBody EtlTask task) {
        return ResponseEntity.ok(etlService.createTask(task));
    }

    @PostMapping("/execute/{taskId}")
    @Operation(summary = "执行任务")
    public ResponseEntity<MockEtlExecutor.TaskExecution> executeTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(etlService.executeTask(taskId));
    }

    @GetMapping("/execute/{taskId}/status")
    @Operation(summary = "获取执行状态")
    public ResponseEntity<MockEtlExecutor.TaskExecution> getExecutionStatus(@PathVariable Long taskId) {
        MockEtlExecutor.TaskExecution execution = etlService.getExecutionStatus(taskId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(execution);
    }

    @PostMapping("/stop/{taskId}")
    @Operation(summary = "停止任务")
    public ResponseEntity<Map<String, Object>> stopTask(@PathVariable Long taskId) {
        boolean stopped = etlService.stopTask(taskId);
        return ResponseEntity.ok(Map.of("success", stopped));
    }

    @GetMapping("/stats")
    @Operation(summary = "任务统计")
    public ResponseEntity<Map<String, Object>> getTaskStats() {
        return ResponseEntity.ok(etlService.getTaskStats());
    }

    @GetMapping("/dependencies")
    @Operation(summary = "获取任务依赖图")
    public ResponseEntity<Map<String, Object>> getTaskDependencies() {
        return ResponseEntity.ok(etlService.getTaskDependencies());
    }
}
