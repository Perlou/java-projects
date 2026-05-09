package com.example.warehouse.service;

import com.example.warehouse.engine.QualityChecker;
import com.example.warehouse.model.*;
import com.example.warehouse.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 数据质量服务
 */
@Service
public class QualityService {

    private static final Logger log = LoggerFactory.getLogger(QualityService.class);

    @Autowired
    private QualityRuleRepository ruleRepository;

    @Autowired
    private QualityResultRepository resultRepository;

    @Autowired
    private TableMetadataRepository tableRepository;

    @Autowired
    private QualityChecker qualityChecker;

    @PostConstruct
    public void init() {
        log.info("QualityService 初始化完成");
        log.info("【数仓知识点】数据质量六维度:");
        log.info("  • 完整性 Completeness");
        log.info("  • 唯一性 Uniqueness");
        log.info("  • 有效性 Validity");
        log.info("  • 一致性 Consistency");
        log.info("  • 时效性 Timeliness");
        log.info("  • 准确性 Accuracy");
    }

    /**
     * 获取所有规则
     */
    public List<QualityRule> getAllRules() {
        return ruleRepository.findAll();
    }

    /**
     * 获取启用的规则
     */
    public List<QualityRule> getEnabledRules() {
        return ruleRepository.findByEnabled(true);
    }

    /**
     * 按表获取规则
     */
    public List<QualityRule> getRulesByTable(Long tableId) {
        return ruleRepository.findByTableId(tableId);
    }

    /**
     * 创建规则
     */
    public QualityRule createRule(QualityRule rule) {
        return ruleRepository.save(rule);
    }

    /**
     * 执行表的质量检测
     */
    public QualityChecker.QualityReport checkTable(Long tableId) {
        List<QualityRule> rules = ruleRepository.findByTableId(tableId);
        List<QualityResult> results = qualityChecker.checkAll(rules);

        // 保存结果
        for (QualityResult result : results) {
            resultRepository.save(result);
        }

        // 生成报告
        QualityChecker.QualityReport report = qualityChecker.generateReport(results);

        // 添加表名
        tableRepository.findById(tableId).ifPresent(table -> {
            for (QualityResult r : report.getResults()) {
                r.setTableName(table.getTableName());
            }
        });

        return report;
    }

    /**
     * 执行所有启用规则的检测
     */
    public QualityChecker.QualityReport checkAll() {
        List<QualityRule> rules = ruleRepository.findByEnabled(true);
        List<QualityResult> results = qualityChecker.checkAll(rules);

        // 保存结果
        for (QualityResult result : results) {
            resultRepository.save(result);
        }

        return qualityChecker.generateReport(results);
    }

    /**
     * 获取表的质量报告
     */
    public List<QualityResult> getTableReport(Long tableId) {
        return resultRepository.findByTableId(tableId);
    }

    /**
     * 获取质量统计
     */
    public Map<String, Object> getQualityStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalRules", ruleRepository.count());
        stats.put("enabledRules", ruleRepository.findByEnabled(true).size());

        // 按类型统计
        Map<String, Long> byType = new LinkedHashMap<>();
        for (QualityRule.RuleType type : QualityRule.RuleType.values()) {
            byType.put(type.name(), (long) ruleRepository.findByRuleType(type).size());
        }
        stats.put("byType", byType);

        // 最近检测结果
        List<QualityResult> recentResults = resultRepository.findAll();
        long passed = recentResults.stream()
                .filter(r -> r.getStatus() == QualityResult.CheckStatus.PASS)
                .count();
        stats.put("recentPassRate", recentResults.isEmpty() ? 0 : passed * 100.0 / recentResults.size());

        return stats;
    }
}
