package com.example.warehouse.controller;

import com.example.warehouse.engine.QualityChecker;
import com.example.warehouse.model.*;
import com.example.warehouse.service.QualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据质量控制器
 */
@RestController
@RequestMapping("/api/quality")
@Tag(name = "数据质量", description = "数据质量检测 API")
public class QualityController {

    @Autowired
    private QualityService qualityService;

    @GetMapping("/rules")
    @Operation(summary = "获取所有规则")
    public ResponseEntity<List<QualityRule>> getAllRules() {
        return ResponseEntity.ok(qualityService.getAllRules());
    }

    @GetMapping("/rules/enabled")
    @Operation(summary = "获取启用的规则")
    public ResponseEntity<List<QualityRule>> getEnabledRules() {
        return ResponseEntity.ok(qualityService.getEnabledRules());
    }

    @GetMapping("/rules/table/{tableId}")
    @Operation(summary = "获取表的规则")
    public ResponseEntity<List<QualityRule>> getRulesByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(qualityService.getRulesByTable(tableId));
    }

    @PostMapping("/rules")
    @Operation(summary = "创建规则")
    public ResponseEntity<QualityRule> createRule(@RequestBody QualityRule rule) {
        return ResponseEntity.ok(qualityService.createRule(rule));
    }

    @PostMapping("/check/table/{tableId}")
    @Operation(summary = "执行表的质量检测")
    public ResponseEntity<QualityChecker.QualityReport> checkTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(qualityService.checkTable(tableId));
    }

    @PostMapping("/check/all")
    @Operation(summary = "执行全部检测")
    public ResponseEntity<QualityChecker.QualityReport> checkAll() {
        return ResponseEntity.ok(qualityService.checkAll());
    }

    @GetMapping("/report/{tableId}")
    @Operation(summary = "获取表的质量报告")
    public ResponseEntity<List<QualityResult>> getTableReport(@PathVariable Long tableId) {
        return ResponseEntity.ok(qualityService.getTableReport(tableId));
    }

    @GetMapping("/stats")
    @Operation(summary = "质量统计")
    public ResponseEntity<Map<String, Object>> getQualityStats() {
        return ResponseEntity.ok(qualityService.getQualityStats());
    }

    @GetMapping("/dimensions")
    @Operation(summary = "获取质量维度定义")
    public ResponseEntity<QualityRule.RuleType[]> getDimensions() {
        return ResponseEntity.ok(QualityRule.RuleType.values());
    }
}
