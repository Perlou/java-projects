package com.example.warehouse.engine;

import com.example.warehouse.model.QualityRule;
import com.example.warehouse.model.QualityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 数据质量检测引擎
 * 
 * 模拟数据质量六维度检测：
 * - 完整性 (Completeness)
 * - 唯一性 (Uniqueness)
 * - 有效性 (Validity)
 * - 一致性 (Consistency)
 * - 时效性 (Timeliness)
 * - 准确性 (Accuracy)
 */
@Component
public class QualityChecker {

    private static final Logger log = LoggerFactory.getLogger(QualityChecker.class);

    private final Random random = new Random();

    @PostConstruct
    public void init() {
        log.info("QualityChecker 初始化完成");
        log.info("【数仓知识点】数据质量检测引擎");
        log.info("  支持维度: 完整性、唯一性、有效性、一致性");
    }

    /**
     * 执行质量检测（模拟）
     */
    public QualityResult check(QualityRule rule) {
        log.info("执行质量规则: {} ({})", rule.getRuleName(), rule.getRuleType());

        QualityResult result = new QualityResult(rule.getId(), rule.getTableId());
        result.setRuleName(rule.getRuleName());

        try {
            // 模拟检测结果
            SimulatedData data = simulateCheck(rule);

            result.setTotalCount(data.totalCount);
            result.setPassCount(data.passCount);
            result.setFailCount(data.failCount);
            result.setPassRate(data.passRate);

            // 判断是否通过
            if (data.passRate >= rule.getThreshold()) {
                result.setStatus(QualityResult.CheckStatus.PASS);
                log.info("✓ 规则 {} 检测通过: {}% >= {}%",
                        rule.getRuleName(), data.passRate, rule.getThreshold());
            } else {
                result.setStatus(QualityResult.CheckStatus.FAIL);
                log.warn("✗ 规则 {} 检测失败: {}% < {}%",
                        rule.getRuleName(), data.passRate, rule.getThreshold());
            }

        } catch (Exception e) {
            result.setStatus(QualityResult.CheckStatus.ERROR);
            result.setErrorMessage(e.getMessage());
            log.error("规则 {} 检测异常: {}", rule.getRuleName(), e.getMessage());
        }

        return result;
    }

    /**
     * 模拟检测数据
     */
    private SimulatedData simulateCheck(QualityRule rule) {
        SimulatedData data = new SimulatedData();

        // 模拟总数据量
        data.totalCount = 10000 + random.nextInt(90000);

        // 根据规则类型模拟不同的通过率
        switch (rule.getRuleType()) {
            case COMPLETENESS:
                // 完整性检查通常通过率较高
                data.passRate = 95.0 + random.nextDouble() * 5;
                break;
            case UNIQUENESS:
                // 唯一性检查
                data.passRate = 98.0 + random.nextDouble() * 2;
                break;
            case VALIDITY:
                // 有效性检查
                data.passRate = 90.0 + random.nextDouble() * 10;
                break;
            case CONSISTENCY:
                // 一致性检查通过率可能较低
                data.passRate = 85.0 + random.nextDouble() * 15;
                break;
            default:
                data.passRate = 95.0;
        }

        // 计算通过/失败数量
        data.passCount = (long) (data.totalCount * data.passRate / 100);
        data.failCount = data.totalCount - data.passCount;

        return data;
    }

    /**
     * 批量检测
     */
    public List<QualityResult> checkAll(List<QualityRule> rules) {
        List<QualityResult> results = new ArrayList<>();

        for (QualityRule rule : rules) {
            if (rule.getEnabled()) {
                results.add(check(rule));
            }
        }

        return results;
    }

    /**
     * 生成质量报告
     */
    public QualityReport generateReport(List<QualityResult> results) {
        QualityReport report = new QualityReport();

        long totalRules = results.size();
        long passedRules = results.stream()
                .filter(r -> r.getStatus() == QualityResult.CheckStatus.PASS)
                .count();
        long failedRules = results.stream()
                .filter(r -> r.getStatus() == QualityResult.CheckStatus.FAIL)
                .count();

        report.setTotalRules(totalRules);
        report.setPassedRules(passedRules);
        report.setFailedRules(failedRules);
        report.setOverallScore(totalRules > 0 ? (passedRules * 100.0 / totalRules) : 0);
        report.setResults(results);

        return report;
    }

    /**
     * 模拟数据内部类
     */
    private static class SimulatedData {
        long totalCount;
        long passCount;
        long failCount;
        double passRate;
    }

    /**
     * 质量报告类
     */
    public static class QualityReport {
        private long totalRules;
        private long passedRules;
        private long failedRules;
        private double overallScore;
        private List<QualityResult> results;

        // Getters and Setters
        public long getTotalRules() {
            return totalRules;
        }

        public void setTotalRules(long totalRules) {
            this.totalRules = totalRules;
        }

        public long getPassedRules() {
            return passedRules;
        }

        public void setPassedRules(long passedRules) {
            this.passedRules = passedRules;
        }

        public long getFailedRules() {
            return failedRules;
        }

        public void setFailedRules(long failedRules) {
            this.failedRules = failedRules;
        }

        public double getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(double overallScore) {
            this.overallScore = overallScore;
        }

        public List<QualityResult> getResults() {
            return results;
        }

        public void setResults(List<QualityResult> results) {
            this.results = results;
        }
    }
}
