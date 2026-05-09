package com.example.analytics.controller;

import com.example.analytics.model.UserAction;
import com.example.analytics.service.DataIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据接入控制器
 * 
 * 提供数据接入的 REST API
 * 
 * @author Java Course
 * 
 */
@RestController
@RequestMapping("/api/data")
@Tag(name = "数据接入", description = "数据采集与管理 API")
public class DataIngestionController {

    private static final Logger log = LoggerFactory.getLogger(DataIngestionController.class);

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    /**
     * 接收单条用户行为数据
     */
    @PostMapping("/ingest")
    @Operation(summary = "接收用户行为数据", description = "接收单条用户行为数据")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody UserAction action) {
        log.info("接收数据: userId={}, action={}", action.getUserId(), action.getActionType());

        dataIngestionService.ingest(action);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "数据接收成功");
        response.put("currentCount", dataIngestionService.getDataCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 批量接收用户行为数据
     */
    @PostMapping("/batch-ingest")
    @Operation(summary = "批量接收数据", description = "批量接收用户行为数据")
    public ResponseEntity<Map<String, Object>> batchIngest(@RequestBody List<UserAction> actions) {
        log.info("批量接收数据: {} 条", actions.size());

        int count = dataIngestionService.batchIngest(actions);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("received", count);
        response.put("currentCount", dataIngestionService.getDataCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 生成测试数据
     */
    @PostMapping("/generate")
    @Operation(summary = "生成测试数据", description = "生成指定数量的模拟测试数据")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "100") int count) {
        log.info("生成测试数据: {} 条", count);

        List<UserAction> generated = dataIngestionService.generateTestData(count);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("generated", generated.size());
        response.put("currentCount", dataIngestionService.getDataCount());
        response.put("sample", generated.size() > 0 ? generated.get(0) : null);

        return ResponseEntity.ok(response);
    }

    /**
     * 生成用户行为路径数据
     */
    @PostMapping("/generate-journey")
    @Operation(summary = "生成用户路径数据", description = "生成指定用户的行为路径数据")
    public ResponseEntity<Map<String, Object>> generateUserJourney(
            @RequestParam(defaultValue = "user_test") String userId,
            @RequestParam(defaultValue = "5") int steps) {
        log.info("生成用户 {} 的行为路径，步数: {}", userId, steps);

        List<UserAction> journey = dataIngestionService.generateUserJourneyData(userId, steps);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("steps", journey.size());
        response.put("journey", journey);

        return ResponseEntity.ok(response);
    }

    /**
     * 获取数据统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取数据统计", description = "获取当前缓冲区的数据统计信息")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", dataIngestionService.getDataCount());
        stats.put("recentSample", dataIngestionService.getRecentData(5));

        return ResponseEntity.ok(stats);
    }

    /**
     * 清空数据缓冲区
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空数据", description = "清空数据缓冲区")
    public ResponseEntity<Map<String, Object>> clearBuffer() {
        dataIngestionService.clearBuffer();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "数据缓冲区已清空");

        return ResponseEntity.ok(response);
    }
}
