package com.example.riskcontrol.engine;

import com.example.riskcontrol.model.RiskRule;
import com.example.riskcontrol.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则引擎
 * 
 * 【Flink 概念对应】
 * 模拟 Flink 的 Broadcast State 机制
 * 
 * 在 Flink 中：
 * - 规则流通过 broadcast() 广播
 * - BroadcastProcessFunction 处理规则更新
 * - 所有并行实例共享相同的规则状态
 * 
 * 这里使用 ConcurrentHashMap 模拟广播状态，支持动态规则更新
 */
@Component
public class RuleEngine {

    private static final Logger log = LoggerFactory.getLogger(RuleEngine.class);

    /**
     * 规则存储
     * 模拟 BroadcastState<String, RiskRule>
     */
    private final Map<String, RiskRule> ruleStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("RuleEngine 初始化完成");
        log.info("【Flink 知识点】模拟 Broadcast State 广播规则");

        // 初始化默认规则
        initDefaultRules();
    }

    /**
     * 初始化默认风控规则
     */
    private void initDefaultRules() {
        // 规则1: 单笔大额交易
        RiskRule highAmount = new RiskRule(
                "RULE_001", "单笔大额交易", "THRESHOLD",
                "amount", "GT", "100000", 8, "ALERT", 20);
        highAmount.setDescription("单笔交易金额超过10万");
        addRule(highAmount);

        // 规则2: 高频交易
        RiskRule highFrequency = new RiskRule(
                "RULE_002", "高频交易", "THRESHOLD",
                "transactionCount1h", "GT", "10", 7, "REVIEW", 15);
        highFrequency.setDescription("1小时内交易次数超过10次");
        addRule(highFrequency);

        // 规则3: 深夜交易
        RiskRule nightTransaction = new RiskRule(
                "RULE_003", "深夜交易", "PATTERN",
                "eventTime", "BETWEEN", "00:00-06:00", 5, "ALERT", 10);
        nightTransaction.setDescription("凌晨0点到6点之间的交易");
        addRule(nightTransaction);

        // 规则4: 新设备交易
        RiskRule newDevice = new RiskRule(
                "RULE_004", "新设备交易", "PATTERN",
                "deviceId", "NOT_IN", "known_devices", 6, "REVIEW", 15);
        newDevice.setDescription("使用未知设备进行交易");
        addRule(newDevice);

        // 规则5: 境外交易
        RiskRule overseas = new RiskRule(
                "RULE_005", "境外交易", "THRESHOLD",
                "country", "NE", "CN", 7, "ALERT", 25);
        overseas.setDescription("在中国以外地区进行交易");
        addRule(overseas);

        log.info("已加载 {} 条默认规则", ruleStore.size());
    }

    /**
     * 添加规则 (模拟 BroadcastState.put())
     */
    public void addRule(RiskRule rule) {
        ruleStore.put(rule.getRuleId(), rule);
        log.info("【Broadcast State Update】规则已广播: {}", rule.getRuleName());
    }

    /**
     * 更新规则
     */
    public void updateRule(RiskRule rule) {
        if (ruleStore.containsKey(rule.getRuleId())) {
            ruleStore.put(rule.getRuleId(), rule);
            log.info("【Broadcast State Update】规则已更新: {}", rule.getRuleName());
        }
    }

    /**
     * 删除规则
     */
    public void removeRule(String ruleId) {
        RiskRule removed = ruleStore.remove(ruleId);
        if (removed != null) {
            log.info("【Broadcast State Update】规则已删除: {}", removed.getRuleName());
        }
    }

    /**
     * 获取规则
     */
    public Optional<RiskRule> getRule(String ruleId) {
        return Optional.ofNullable(ruleStore.get(ruleId));
    }

    /**
     * 获取所有规则
     */
    public List<RiskRule> getAllRules() {
        return new ArrayList<>(ruleStore.values());
    }

    /**
     * 获取启用的规则（按优先级排序）
     */
    public List<RiskRule> getEnabledRules() {
        return ruleStore.values().stream()
                .filter(RiskRule::isEnabled)
                .sorted(Comparator.comparingInt(RiskRule::getPriority).reversed())
                .toList();
    }

    /**
     * 匹配规则
     * 
     * 【Flink 知识点】
     * 在 BroadcastProcessFunction.processElement() 中
     * 遍历广播状态中的规则进行匹配
     */
    public List<RuleMatchResult> matchRules(Transaction tx) {
        List<RuleMatchResult> results = new ArrayList<>();

        for (RiskRule rule : getEnabledRules()) {
            if (matchRule(tx, rule)) {
                log.debug("交易 {} 匹配规则: {}", tx.getTransactionId(), rule.getRuleName());
                results.add(new RuleMatchResult(rule, tx));
            }
        }

        return results;
    }

    /**
     * 单条规则匹配
     */
    private boolean matchRule(Transaction tx, RiskRule rule) {
        try {
            String targetField = rule.getTargetField();
            String operator = rule.getOperator();
            String threshold = rule.getThreshold();

            switch (targetField) {
                case "amount" -> {
                    BigDecimal txAmount = tx.getAmount();
                    BigDecimal thresholdAmount = new BigDecimal(threshold);
                    return compareAmount(txAmount, thresholdAmount, operator);
                }
                case "country" -> {
                    return compareString(tx.getCountry(), threshold, operator);
                }
                case "city" -> {
                    return compareString(tx.getCity(), threshold, operator);
                }
                case "eventTime" -> {
                    // 检查时间范围
                    if ("BETWEEN".equals(operator)) {
                        return isTimeInRange(tx.getEventTime().getHour(), threshold);
                    }
                }
                case "channel" -> {
                    return compareString(tx.getChannel(), threshold, operator);
                }
            }
        } catch (Exception e) {
            log.error("规则匹配异常: {}", e.getMessage());
        }

        return false;
    }

    private boolean compareAmount(BigDecimal value, BigDecimal threshold, String operator) {
        int cmp = value.compareTo(threshold);
        return switch (operator) {
            case "GT" -> cmp > 0;
            case "GE" -> cmp >= 0;
            case "LT" -> cmp < 0;
            case "LE" -> cmp <= 0;
            case "EQ" -> cmp == 0;
            default -> false;
        };
    }

    private boolean compareString(String value, String threshold, String operator) {
        if (value == null)
            return false;
        return switch (operator) {
            case "EQ" -> value.equals(threshold);
            case "NE" -> !value.equals(threshold);
            case "IN" -> Arrays.asList(threshold.split(",")).contains(value);
            case "NOT_IN" -> !Arrays.asList(threshold.split(",")).contains(value);
            default -> false;
        };
    }

    private boolean isTimeInRange(int hour, String range) {
        // range 格式: "00:00-06:00"
        String[] parts = range.split("-");
        if (parts.length == 2) {
            int start = Integer.parseInt(parts[0].split(":")[0]);
            int end = Integer.parseInt(parts[1].split(":")[0]);
            if (start <= end) {
                return hour >= start && hour < end;
            } else {
                // 跨午夜
                return hour >= start || hour < end;
            }
        }
        return false;
    }

    /**
     * 规则匹配结果
     */
    public static class RuleMatchResult {
        private final RiskRule rule;
        private final Transaction transaction;

        public RuleMatchResult(RiskRule rule, Transaction transaction) {
            this.rule = rule;
            this.transaction = transaction;
        }

        public RiskRule getRule() {
            return rule;
        }

        public Transaction getTransaction() {
            return transaction;
        }
    }
}
