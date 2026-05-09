package com.example.warehouse.service;

import com.example.warehouse.engine.SqlLineageParser;
import com.example.warehouse.model.*;
import com.example.warehouse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 数据血缘服务
 */
@Service
public class LineageService {

    private static final Logger log = LoggerFactory.getLogger(LineageService.class);

    @Autowired
    private LineageEdgeRepository lineageRepository;

    @Autowired
    private TableMetadataRepository tableRepository;

    @Autowired
    private SqlLineageParser sqlLineageParser;

    @PostConstruct
    public void init() {
        log.info("LineageService 初始化完成");
        log.info("【数仓知识点】数据血缘:");
        log.info("  • 上游血缘 (Upstream): 数据从哪来");
        log.info("  • 下游血缘 (Downstream): 数据去哪里");
        log.info("  • 影响分析: 变更影响范围");
    }

    /**
     * 获取所有血缘关系
     */
    public List<LineageEdge> getAllLineage() {
        List<LineageEdge> edges = lineageRepository.findAll();
        enrichEdges(edges);
        return edges;
    }

    /**
     * 获取表的血缘（上下游）
     */
    public Map<String, Object> getTableLineage(Long tableId) {
        Map<String, Object> lineage = new LinkedHashMap<>();

        Optional<TableMetadata> tableOpt = tableRepository.findById(tableId);
        if (tableOpt.isEmpty()) {
            return lineage;
        }

        TableMetadata table = tableOpt.get();
        lineage.put("table", table.getTableName());
        lineage.put("layer", table.getDwLayer().name());

        // 上游
        List<LineageEdge> upstream = lineageRepository.findByTargetTableId(tableId);
        enrichEdges(upstream);
        lineage.put("upstream", upstream);

        // 下游
        List<LineageEdge> downstream = lineageRepository.findBySourceTableId(tableId);
        enrichEdges(downstream);
        lineage.put("downstream", downstream);

        return lineage;
    }

    /**
     * 获取上游血缘（递归）
     */
    public List<LineageEdge> getUpstreamLineage(Long tableId) {
        Set<Long> visited = new HashSet<>();
        List<LineageEdge> result = new ArrayList<>();
        collectUpstream(tableId, visited, result);
        enrichEdges(result);
        return result;
    }

    private void collectUpstream(Long tableId, Set<Long> visited, List<LineageEdge> result) {
        if (visited.contains(tableId))
            return;
        visited.add(tableId);

        List<LineageEdge> edges = lineageRepository.findByTargetTableId(tableId);
        for (LineageEdge edge : edges) {
            result.add(edge);
            collectUpstream(edge.getSourceTableId(), visited, result);
        }
    }

    /**
     * 获取下游血缘（递归）
     */
    public List<LineageEdge> getDownstreamLineage(Long tableId) {
        Set<Long> visited = new HashSet<>();
        List<LineageEdge> result = new ArrayList<>();
        collectDownstream(tableId, visited, result);
        enrichEdges(result);
        return result;
    }

    private void collectDownstream(Long tableId, Set<Long> visited, List<LineageEdge> result) {
        if (visited.contains(tableId))
            return;
        visited.add(tableId);

        List<LineageEdge> edges = lineageRepository.findBySourceTableId(tableId);
        for (LineageEdge edge : edges) {
            result.add(edge);
            collectDownstream(edge.getTargetTableId(), visited, result);
        }
    }

    /**
     * 解析 SQL 提取血缘
     */
    public SqlLineageParser.LineageParseResult parseSql(String sql) {
        return sqlLineageParser.parse(sql);
    }

    /**
     * 保存血缘边
     */
    public LineageEdge saveLineage(LineageEdge edge) {
        return lineageRepository.save(edge);
    }

    /**
     * 获取血缘图数据（用于可视化）
     */
    public Map<String, Object> getLineageGraph() {
        Map<String, Object> graph = new LinkedHashMap<>();

        // 节点
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (TableMetadata table : tableRepository.findAll()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", table.getId());
            node.put("name", table.getTableName());
            node.put("layer", table.getDwLayer().name());
            node.put("layerName", table.getDwLayer().getDisplayName());
            nodes.add(node);
        }
        graph.put("nodes", nodes);

        // 边
        List<Map<String, Object>> edges = new ArrayList<>();
        for (LineageEdge edge : lineageRepository.findAll()) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("source", edge.getSourceTableId());
            e.put("target", edge.getTargetTableId());
            e.put("transformation", edge.getTransformation());
            edges.add(e);
        }
        graph.put("edges", edges);

        return graph;
    }

    /**
     * 丰富血缘边信息（添加表名等）
     */
    private void enrichEdges(List<LineageEdge> edges) {
        for (LineageEdge edge : edges) {
            tableRepository.findById(edge.getSourceTableId()).ifPresent(t -> {
                edge.setSourceTableName(t.getTableName());
                edge.setSourceTableLayer(t.getDwLayer().name());
            });
            tableRepository.findById(edge.getTargetTableId()).ifPresent(t -> {
                edge.setTargetTableName(t.getTableName());
                edge.setTargetTableLayer(t.getDwLayer().name());
            });
        }
    }
}
