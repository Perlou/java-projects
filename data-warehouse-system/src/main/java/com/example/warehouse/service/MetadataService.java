package com.example.warehouse.service;

import com.example.warehouse.model.*;
import com.example.warehouse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 元数据服务
 */
@Service
public class MetadataService {

    private static final Logger log = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private TableMetadataRepository tableRepository;

    @Autowired
    private ColumnMetadataRepository columnRepository;

    @PostConstruct
    public void init() {
        log.info("MetadataService 初始化完成");
    }

    /**
     * 获取所有表
     */
    public List<TableMetadata> getAllTables() {
        return tableRepository.findAll();
    }

    /**
     * 按分层获取表
     */
    public List<TableMetadata> getTablesByLayer(DwLayer layer) {
        return tableRepository.findByDwLayer(layer);
    }

    /**
     * 获取表详情
     */
    public Optional<TableMetadata> getTable(Long id) {
        return tableRepository.findById(id);
    }

    /**
     * 按名称获取表
     */
    public Optional<TableMetadata> getTableByName(String tableName) {
        return tableRepository.findByTableName(tableName);
    }

    /**
     * 创建表
     */
    public TableMetadata createTable(TableMetadata table) {
        return tableRepository.save(table);
    }

    /**
     * 获取表的列
     */
    public List<ColumnMetadata> getColumns(Long tableId) {
        return columnRepository.findByTableIdOrderByOrdinalPosition(tableId);
    }

    /**
     * 获取分层统计
     */
    public Map<String, Object> getLayerStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        for (DwLayer layer : DwLayer.values()) {
            List<TableMetadata> tables = tableRepository.findByDwLayer(layer);
            Map<String, Object> layerInfo = new LinkedHashMap<>();
            layerInfo.put("displayName", layer.getDisplayName());
            layerInfo.put("description", layer.getDescription());
            layerInfo.put("tableCount", tables.size());
            layerInfo.put("tables", tables.stream()
                    .map(TableMetadata::getTableName)
                    .collect(Collectors.toList()));
            stats.put(layer.name(), layerInfo);
        }

        return stats;
    }

    /**
     * 获取数仓架构概览
     */
    public Map<String, Object> getArchitectureOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        long totalTables = tableRepository.count();
        overview.put("totalTables", totalTables);

        Map<String, Long> layerCounts = Arrays.stream(DwLayer.values())
                .collect(Collectors.toMap(
                        DwLayer::name,
                        layer -> (long) tableRepository.findByDwLayer(layer).size()));
        overview.put("layerCounts", layerCounts);

        // 数仓分层流程
        List<String> dataFlow = List.of(
                "数据源 → ODS (原始数据层)",
                "ODS → DIM (维度表层)",
                "ODS + DIM → DWD (明细数据层)",
                "DWD → DWS (服务数据层)",
                "DWS → ADS (应用数据层)");
        overview.put("dataFlow", dataFlow);

        return overview;
    }
}
