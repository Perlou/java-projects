package com.example.warehouse.engine;

import com.example.warehouse.model.LineageEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.*;

/**
 * SQL 血缘解析器
 * 
 * 模拟从 SQL 语句中提取表级和列级血缘关系
 * 
 * 支持的 SQL 类型：
 * - INSERT INTO ... SELECT
 * - CREATE TABLE ... AS SELECT
 * - INSERT OVERWRITE ... SELECT
 */
@Component
public class SqlLineageParser {

    private static final Logger log = LoggerFactory.getLogger(SqlLineageParser.class);

    // SQL 关键字正则
    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "INSERT\\s+(INTO|OVERWRITE)\\s+(TABLE\\s+)?(\\w+\\.?\\w*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREATE_AS_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(\\w+\\.?\\w*)\\s+AS",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern FROM_PATTERN = Pattern.compile(
            "FROM\\s+(\\w+\\.?\\w*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern JOIN_PATTERN = Pattern.compile(
            "JOIN\\s+(\\w+\\.?\\w*)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SELECT_COLUMNS_PATTERN = Pattern.compile(
            "SELECT\\s+(.+?)\\s+FROM",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @PostConstruct
    public void init() {
        log.info("SqlLineageParser 初始化完成");
        log.info("【数仓知识点】SQL 血缘解析器");
        log.info("  支持: INSERT INTO/OVERWRITE, CREATE TABLE AS SELECT");
        log.info("  提取: 表级血缘 + 列级血缘");
    }

    /**
     * 解析 SQL 提取血缘信息
     */
    public LineageParseResult parse(String sql) {
        log.debug("解析 SQL: {}", sql);

        LineageParseResult result = new LineageParseResult();
        result.setOriginalSql(sql);

        // 提取目标表
        String targetTable = extractTargetTable(sql);
        result.setTargetTable(targetTable);

        // 提取源表
        Set<String> sourceTables = extractSourceTables(sql);
        result.setSourceTables(new ArrayList<>(sourceTables));

        // 提取列映射（简化版）
        Map<String, String> columnMapping = extractColumnMapping(sql);
        result.setColumnMapping(columnMapping);

        // 生成血缘边
        for (String sourceTable : sourceTables) {
            LineageEdge edge = new LineageEdge();
            edge.setSourceTableName(sourceTable);
            edge.setTargetTableName(targetTable);

            // 简化的列映射
            StringBuilder sourceCols = new StringBuilder();
            StringBuilder targetCols = new StringBuilder();
            for (Map.Entry<String, String> entry : columnMapping.entrySet()) {
                if (entry.getValue().contains(sourceTable) || !entry.getValue().contains(".")) {
                    if (sourceCols.length() > 0) {
                        sourceCols.append(",");
                        targetCols.append(",");
                    }
                    sourceCols.append(entry.getValue());
                    targetCols.append(entry.getKey());
                }
            }
            edge.setSourceColumn(sourceCols.toString());
            edge.setTargetColumn(targetCols.toString());
            edge.setTransformation("SQL_PARSE");

            result.getEdges().add(edge);
        }

        log.info("血缘解析完成: {} -> {}", sourceTables, targetTable);
        return result;
    }

    /**
     * 提取目标表
     */
    private String extractTargetTable(String sql) {
        // INSERT INTO/OVERWRITE
        Matcher insertMatcher = INSERT_PATTERN.matcher(sql);
        if (insertMatcher.find()) {
            return insertMatcher.group(3);
        }

        // CREATE TABLE AS
        Matcher createMatcher = CREATE_AS_PATTERN.matcher(sql);
        if (createMatcher.find()) {
            return createMatcher.group(1);
        }

        return null;
    }

    /**
     * 提取源表
     */
    private Set<String> extractSourceTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();

        // FROM clause
        Matcher fromMatcher = FROM_PATTERN.matcher(sql);
        while (fromMatcher.find()) {
            tables.add(fromMatcher.group(1));
        }

        // JOIN clause
        Matcher joinMatcher = JOIN_PATTERN.matcher(sql);
        while (joinMatcher.find()) {
            tables.add(joinMatcher.group(1));
        }

        return tables;
    }

    /**
     * 提取列映射（简化版本）
     */
    private Map<String, String> extractColumnMapping(String sql) {
        Map<String, String> mapping = new LinkedHashMap<>();

        Matcher selectMatcher = SELECT_COLUMNS_PATTERN.matcher(sql);
        if (selectMatcher.find()) {
            String columnsPart = selectMatcher.group(1).trim();

            if ("*".equals(columnsPart)) {
                mapping.put("*", "*");
                return mapping;
            }

            // 分割列
            String[] columns = columnsPart.split(",");
            for (String col : columns) {
                col = col.trim();

                // 处理 AS 别名
                String[] parts = col.split("(?i)\\s+AS\\s+");
                if (parts.length == 2) {
                    mapping.put(parts[1].trim(), parts[0].trim());
                } else {
                    // 无别名，使用原始列名
                    String colName = col.contains(".")
                            ? col.substring(col.lastIndexOf('.') + 1)
                            : col;
                    mapping.put(colName, col);
                }
            }
        }

        return mapping;
    }

    /**
     * 血缘解析结果
     */
    public static class LineageParseResult {
        private String originalSql;
        private String targetTable;
        private List<String> sourceTables = new ArrayList<>();
        private Map<String, String> columnMapping = new LinkedHashMap<>();
        private List<LineageEdge> edges = new ArrayList<>();

        // Getters and Setters
        public String getOriginalSql() {
            return originalSql;
        }

        public void setOriginalSql(String originalSql) {
            this.originalSql = originalSql;
        }

        public String getTargetTable() {
            return targetTable;
        }

        public void setTargetTable(String targetTable) {
            this.targetTable = targetTable;
        }

        public List<String> getSourceTables() {
            return sourceTables;
        }

        public void setSourceTables(List<String> sourceTables) {
            this.sourceTables = sourceTables;
        }

        public Map<String, String> getColumnMapping() {
            return columnMapping;
        }

        public void setColumnMapping(Map<String, String> columnMapping) {
            this.columnMapping = columnMapping;
        }

        public List<LineageEdge> getEdges() {
            return edges;
        }

        public void setEdges(List<LineageEdge> edges) {
            this.edges = edges;
        }
    }
}
