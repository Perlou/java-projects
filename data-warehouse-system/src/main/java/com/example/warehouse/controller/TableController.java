package com.example.warehouse.controller;

import com.example.warehouse.model.*;
import com.example.warehouse.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 表管理控制器
 */
@RestController
@RequestMapping("/api/tables")
@Tag(name = "表管理", description = "数仓表元数据管理 API")
public class TableController {

    @Autowired
    private MetadataService metadataService;

    @GetMapping
    @Operation(summary = "获取所有表")
    public ResponseEntity<List<TableMetadata>> getAllTables() {
        return ResponseEntity.ok(metadataService.getAllTables());
    }

    @GetMapping("/layer/{layer}")
    @Operation(summary = "按分层获取表")
    public ResponseEntity<List<TableMetadata>> getTablesByLayer(@PathVariable DwLayer layer) {
        return ResponseEntity.ok(metadataService.getTablesByLayer(layer));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取表详情")
    public ResponseEntity<TableMetadata> getTable(@PathVariable Long id) {
        return metadataService.getTable(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/columns")
    @Operation(summary = "获取表的列")
    public ResponseEntity<List<ColumnMetadata>> getColumns(@PathVariable Long id) {
        return ResponseEntity.ok(metadataService.getColumns(id));
    }

    @PostMapping
    @Operation(summary = "创建表")
    public ResponseEntity<TableMetadata> createTable(@RequestBody TableMetadata table) {
        return ResponseEntity.ok(metadataService.createTable(table));
    }

    @GetMapping("/stats/layers")
    @Operation(summary = "分层统计")
    public ResponseEntity<Map<String, Object>> getLayerStats() {
        return ResponseEntity.ok(metadataService.getLayerStats());
    }

    @GetMapping("/architecture")
    @Operation(summary = "数仓架构概览")
    public ResponseEntity<Map<String, Object>> getArchitectureOverview() {
        return ResponseEntity.ok(metadataService.getArchitectureOverview());
    }

    @GetMapping("/layers")
    @Operation(summary = "获取分层定义")
    public ResponseEntity<DwLayer[]> getLayers() {
        return ResponseEntity.ok(DwLayer.values());
    }
}
