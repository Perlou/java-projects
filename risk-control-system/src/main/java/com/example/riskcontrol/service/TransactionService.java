package com.example.riskcontrol.service;

import com.example.riskcontrol.engine.PatternMatcher;
import com.example.riskcontrol.engine.RiskScoreCalculator;
import com.example.riskcontrol.engine.RuleEngine;
import com.example.riskcontrol.model.*;
import com.example.riskcontrol.processor.StateManager;
import com.example.riskcontrol.processor.WindowAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 交易服务
 * 
 * 处理交易事件，进行风控检测
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private StateManager stateManager;

    @Autowired
    private PatternMatcher patternMatcher;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private WindowAggregator windowAggregator;

    @Autowired
    private RiskScoreCalculator riskScoreCalculator;

    @Autowired
    private AlertService alertService;

    /**
     * 处理交易事件
     * 
     * 【Flink 数据流处理流程】
     * 1. 接收事件
     * 2. 更新状态 (State)
     * 3. 窗口聚合 (Window)
     * 4. 规则匹配 (Broadcast State)
     * 5. 模式匹配 (CEP)
     * 6. 生成告警
     */
    public TransactionResult processTransaction(Transaction tx) {
        String userId = tx.getUserId();
        log.info("处理交易: {} 用户: {} 金额: {}",
                tx.getTransactionId(), userId, tx.getAmount());

        TransactionResult result = new TransactionResult();
        result.setTransactionId(tx.getTransactionId());
        result.setUserId(userId);
        result.setProcessTime(LocalDateTime.now());

        // 1. 更新状态（模拟 Flink State）
        stateManager.addTransaction(userId, tx);
        UserRiskProfile profile = stateManager.getUserProfile(userId);
        profile.recordTransaction(tx);

        // 2. 窗口聚合（模拟 Flink Window）
        WindowAggregator.WindowStats windowStats = windowAggregator.aggregateTumblingWindow(userId, tx);
        windowAggregator.updateGlobalStats(userId, tx);
        result.setWindowStats(windowStats);

        // 3. 规则匹配（模拟 Broadcast State）
        List<RuleEngine.RuleMatchResult> ruleMatches = ruleEngine.matchRules(tx);
        result.setMatchedRules(ruleMatches.size());

        // 4. CEP 模式匹配
        List<Transaction> txHistory = stateManager.getTransactionHistory(userId);
        List<LoginEvent> loginHistory = stateManager.getLoginHistory(userId);
        List<Alert> patternAlerts = patternMatcher.matchAllPatterns(
                userId, txHistory, loginHistory);

        // 5. 根据规则生成告警
        List<Alert> ruleAlerts = new ArrayList<>();
        for (RuleEngine.RuleMatchResult match : ruleMatches) {
            Alert alert = createAlertFromRule(match);
            ruleAlerts.add(alert);
        }

        // 6. 合并所有告警
        List<Alert> allAlerts = new ArrayList<>();
        allAlerts.addAll(patternAlerts);
        allAlerts.addAll(ruleAlerts);

        // 7. 保存告警并更新风险分数
        for (Alert alert : allAlerts) {
            alertService.addAlert(alert);
        }

        if (!allAlerts.isEmpty()) {
            int newScore = riskScoreCalculator.calculateFromAlerts(profile, allAlerts);
            profile.setRiskScore(newScore);
            profile.setAlertCount(profile.getAlertCount() + allAlerts.size());
            profile.setLastAlertTime(LocalDateTime.now());
        }

        // 8. 更新用户画像
        stateManager.updateUserProfile(userId, profile);

        // 9. 设置结果
        result.setAlerts(allAlerts);
        result.setRiskScore(profile.getRiskScore());
        result.setRiskLevel(profile.getRiskLevel());
        result.setBlocked(profile.getRiskScore() >= 80 ||
                allAlerts.stream().anyMatch(a -> a.getLevel() == Alert.AlertLevel.CRITICAL));

        log.info("交易处理完成: {} 告警数: {} 风险分数: {} 是否阻断: {}",
                tx.getTransactionId(), allAlerts.size(),
                profile.getRiskScore(), result.isBlocked());

        return result;
    }

    /**
     * 处理登录事件
     */
    public LoginResult processLoginEvent(LoginEvent event) {
        String userId = event.getUserId();
        log.info("处理登录事件: {} 用户: {} 状态: {}",
                event.getEventId(), userId, event.getStatus());

        // 更新状态
        stateManager.addLoginEvent(userId, event);
        UserRiskProfile profile = stateManager.getUserProfile(userId);

        if (event.isFailed()) {
            profile.setLoginFailCount(profile.getLoginFailCount() + 1);
        } else {
            profile.setLoginFailCount(0); // 成功登录重置计数
            profile.setLastLoginTime(event.getEventTime());
        }

        // CEP 模式匹配（仅登录相关）
        List<LoginEvent> loginHistory = stateManager.getLoginHistory(userId);
        Optional<Alert> loginAlert = patternMatcher.matchLoginFailurePattern(userId, loginHistory);

        LoginResult result = new LoginResult();
        result.setEventId(event.getEventId());
        result.setUserId(userId);

        if (loginAlert.isPresent()) {
            Alert alert = loginAlert.get();
            alertService.addAlert(alert);
            profile.addRiskScore(alert.getRiskScore());
            profile.setAlertCount(profile.getAlertCount() + 1);
            profile.setLastAlertTime(LocalDateTime.now());

            result.setAlert(alert);
            result.setBlocked(true);
        }

        stateManager.updateUserProfile(userId, profile);
        result.setRiskScore(profile.getRiskScore());

        return result;
    }

    private Alert createAlertFromRule(RuleEngine.RuleMatchResult match) {
        RiskRule rule = match.getRule();
        Transaction tx = match.getTransaction();

        Alert alert = new Alert();
        alert.setAlertId(UUID.randomUUID().toString());
        alert.setUserId(tx.getUserId());
        alert.setAlertType("RULE_" + rule.getRuleType());
        alert.setRuleId(rule.getRuleId());
        alert.setMessage(String.format("交易 %s 触发规则 [%s]: %s",
                tx.getTransactionId(), rule.getRuleName(), rule.getDescription()));
        alert.setRiskScore(rule.getRiskScore());
        alert.setTriggerTime(LocalDateTime.now());
        alert.setMatchedEventIds(List.of(tx.getTransactionId()));

        // 根据优先级设置告警级别
        if (rule.getPriority() >= 8) {
            alert.setLevel(Alert.AlertLevel.CRITICAL);
        } else if (rule.getPriority() >= 6) {
            alert.setLevel(Alert.AlertLevel.HIGH);
        } else if (rule.getPriority() >= 4) {
            alert.setLevel(Alert.AlertLevel.MEDIUM);
        } else {
            alert.setLevel(Alert.AlertLevel.LOW);
        }

        return alert;
    }

    /**
     * 生成测试交易数据
     */
    public List<Transaction> generateTestTransactions(int count) {
        List<Transaction> transactions = new ArrayList<>();
        String[] users = { "user001", "user002", "user003", "user004", "user005" };
        String[] cities = { "北京", "上海", "广州", "深圳", "杭州" };
        String[] types = { "PAYMENT", "TRANSFER", "WITHDRAWAL" };
        String[] channels = { "APP", "WEB", "POS" };
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            Transaction tx = new Transaction();
            tx.setTransactionId("TX" + System.currentTimeMillis() + "-" + i);
            tx.setUserId(users[random.nextInt(users.length)]);
            tx.setMerchantId("M" + random.nextInt(100));
            tx.setAmount(BigDecimal.valueOf(random.nextInt(100000) + 100));
            tx.setCurrency("CNY");
            tx.setCity(cities[random.nextInt(cities.length)]);
            tx.setCountry("CN");
            tx.setDeviceId("D" + random.nextInt(10));
            tx.setIpAddress("192.168.1." + random.nextInt(255));
            tx.setTransactionType(types[random.nextInt(types.length)]);
            tx.setChannel(channels[random.nextInt(channels.length)]);
            tx.setEventTime(LocalDateTime.now().minusMinutes(random.nextInt(60)));

            transactions.add(tx);
        }

        return transactions;
    }

    // 结果类
    public static class TransactionResult {
        private String transactionId;
        private String userId;
        private LocalDateTime processTime;
        private int riskScore;
        private String riskLevel;
        private boolean blocked;
        private int matchedRules;
        private List<Alert> alerts = new ArrayList<>();
        private WindowAggregator.WindowStats windowStats;

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public LocalDateTime getProcessTime() {
            return processTime;
        }

        public void setProcessTime(LocalDateTime processTime) {
            this.processTime = processTime;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(int riskScore) {
            this.riskScore = riskScore;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

        public int getMatchedRules() {
            return matchedRules;
        }

        public void setMatchedRules(int matchedRules) {
            this.matchedRules = matchedRules;
        }

        public List<Alert> getAlerts() {
            return alerts;
        }

        public void setAlerts(List<Alert> alerts) {
            this.alerts = alerts;
        }

        public WindowAggregator.WindowStats getWindowStats() {
            return windowStats;
        }

        public void setWindowStats(WindowAggregator.WindowStats windowStats) {
            this.windowStats = windowStats;
        }
    }

    public static class LoginResult {
        private String eventId;
        private String userId;
        private int riskScore;
        private boolean blocked;
        private Alert alert;

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(int riskScore) {
            this.riskScore = riskScore;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

        public Alert getAlert() {
            return alert;
        }

        public void setAlert(Alert alert) {
            this.alert = alert;
        }
    }
}
