package com.example.warehouse.controller;

import com.example.warehouse.engine.SqlLineageParser;
import com.example.warehouse.model.LineageEdge;
import com.example.warehouse.service.LineageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据血缘控制器
 */
@RestController
@RequestMapping("/api/lineage")
@Tag(name = "数据血缘", description = "数据血缘追踪 API")
public class LineageController {

    @Autowired
    private LineageService lineageService;

    @GetMapping
    @Operation(summary = "获取所有血缘关系")
    public ResponseEntity<List<LineageEdge>> getAllLineage() {
        return ResponseEntity.ok(lineageService.getAllLineage());
    }

    @GetMapping("/{tableId}")
    @Operation(summary = "获取表的血缘（上下游）")
    public ResponseEntity<Map<String, Object>> getTableLineage(@PathVariable Long tableId) {
        return ResponseEntity.ok(lineageService.getTableLineage(tableId));
    }

    @GetMapping("/{tableId}/upstream")
    @Operation(summary = "获取上游血缘（递归）")
    public ResponseEntity<List<LineageEdge>> getUpstreamLineage(@PathVariable Long tableId) {
        return ResponseEntity.ok(lineageService.getUpstreamLineage(tableId));
    }

    @GetMapping("/{tableId}/downstream")
    @Operation(summary = "获取下游血缘（递归）")
    public ResponseEntity<List<LineageEdge>> getDownstreamLineage(@PathVariable Long tableId) {
        return ResponseEntity.ok(lineageService.getDownstreamLineage(tableId));
    }

    @PostMapping("/parse")
    @Operation(summary = "解析 SQL 提取血缘", description = "从 SQL 语句中提取表级和列级血缘")
    public ResponseEntity<SqlLineageParser.LineageParseResult> parseSql(
            @RequestBody SqlParseRequest request) {
        return ResponseEntity.ok(lineageService.parseSql(request.getSql()));
    }

    @PostMapping
    @Operation(summary = "保存血缘关系")
    public ResponseEntity<LineageEdge> saveLineage(@RequestBody LineageEdge edge) {
        return ResponseEntity.ok(lineageService.saveLineage(edge));
    }

    @GetMapping("/graph")
    @Operation(summary = "获取血缘图数据", description = "用于可视化展示")
    public ResponseEntity<Map<String, Object>> getLineageGraph() {
        return ResponseEntity.ok(lineageService.getLineageGraph());
    }

    /**
     * SQL 解析请求
     */
    public static class SqlParseRequest {
        private String sql;

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }
    }
}
