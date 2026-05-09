package com.example.riskcontrol.engine;

import com.example.riskcontrol.model.Alert;
import com.example.riskcontrol.model.LoginEvent;
import com.example.riskcontrol.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CEP 模式匹配引擎
 * 
 * 【Flink CEP 概念对应】
 * 模拟 Flink CEP 的模式定义与匹配：
 * 
 * Pattern API:
 * - begin(): 模式开始
 * - next(): 严格紧邻
 * - followedBy(): 宽松近邻
 * - within(): 时间约束
 * - where(): 条件过滤
 * - times(n): 量词
 * 
 * PatternProcessFunction:
 * - processMatch(): 处理匹配结果
 * - processTimedOutMatch(): 处理超时
 */
@Component
public class PatternMatcher {

    private static final Logger log = LoggerFactory.getLogger(PatternMatcher.class);

    @Value("${riskcontrol.cep.login-failure.count:3}")
    private int loginFailureCount;

    @Value("${riskcontrol.cep.login-failure.window-minutes:5}")
    private int loginFailureWindowMinutes;

    @Value("${riskcontrol.cep.cross-city.window-minutes:10}")
    private int crossCityWindowMinutes;

    @Value("${riskcontrol.cep.high-amount.threshold:50000}")
    private BigDecimal highAmountThreshold;

    @Value("${riskcontrol.cep.high-amount.count:3}")
    private int highAmountCount;

    @Value("${riskcontrol.cep.high-amount.window-minutes:30}")
    private int highAmountWindowMinutes;

    @PostConstruct
    public void init() {
        log.info("PatternMatcher 初始化完成");
        log.info("【Flink CEP 概念】已注册以下模式:");
        log.info("  • LoginFailurePattern: 连续{}次登录失败在{}分钟内",
                loginFailureCount, loginFailureWindowMinutes);
        log.info("  • CrossCityPattern: {}分钟内异地交易", crossCityWindowMinutes);
        log.info("  • HighAmountPattern: {}分钟内{}笔超过{}的交易",
                highAmountWindowMinutes, highAmountCount, highAmountThreshold);
    }

    /**
     * 模式1: 连续登录失败检测
     * 
     * 【Flink CEP 实现】
     * Pattern.<LoginEvent>begin("first")
     * .where(e -> e.getStatus().equals("FAIL"))
     * .next("second").where(e -> e.getStatus().equals("FAIL"))
     * .next("third").where(e -> e.getStatus().equals("FAIL"))
     * .within(Time.minutes(5))
     */
    public Optional<Alert> matchLoginFailurePattern(String userId, List<LoginEvent> events) {
        log.debug("检查用户 {} 的登录失败模式，事件数: {}", userId, events.size());

        if (events.size() < loginFailureCount) {
            return Optional.empty();
        }

        // 筛选时间窗口内的失败事件
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(loginFailureWindowMinutes);

        List<LoginEvent> failedEvents = events.stream()
                .filter(e -> e.isFailed())
                .filter(e -> e.getEventTime().isAfter(windowStart))
                .sorted(Comparator.comparing(LoginEvent::getEventTime))
                .toList();

        // 模拟 Pattern.next() - 检查连续性
        if (failedEvents.size() >= loginFailureCount) {
            // 检查最近 N 次是否连续（严格紧邻）
            List<LoginEvent> recentFails = failedEvents.subList(
                    failedEvents.size() - loginFailureCount, failedEvents.size());

            // 验证时间窗口
            LocalDateTime first = recentFails.get(0).getEventTime();
            LocalDateTime last = recentFails.get(recentFails.size() - 1).getEventTime();

            if (Duration.between(first, last).toMinutes() <= loginFailureWindowMinutes) {
                log.warn("【CEP 匹配成功】用户 {} 连续登录失败 {} 次", userId, loginFailureCount);

                Alert alert = new Alert();
                alert.setAlertId(UUID.randomUUID().toString());
                alert.setUserId(userId);
                alert.setAlertType("LOGIN_FAILURE");
                alert.setLevel(Alert.AlertLevel.HIGH);
                alert.setPatternName("LoginFailurePattern");
                alert.setMessage(String.format("用户 %s 在 %d 分钟内连续登录失败 %d 次，疑似暴力破解",
                        userId, loginFailureWindowMinutes, loginFailureCount));
                alert.setWindowStart(first);
                alert.setWindowEnd(last);
                alert.setRiskScore(30);
                alert.setMatchedEventIds(
                        recentFails.stream().map(LoginEvent::getEventId).toList());

                return Optional.of(alert);
            }
        }

        return Optional.empty();
    }

    /**
     * 模式2: 异地交易检测
     * 
     * 【Flink CEP 实现】
     * Pattern.<Transaction>begin("first")
     * .followedBy("second")
     * .where(new IterativeCondition<Transaction>() {
     * public boolean filter(Transaction tx, Context<Transaction> ctx) {
     * Transaction first = ctx.getEventsForPattern("first").get(0);
     * return !tx.getCity().equals(first.getCity());
     * }
     * })
     * .within(Time.minutes(10))
     */
    public Optional<Alert> matchCrossCityPattern(String userId, List<Transaction> transactions) {
        log.debug("检查用户 {} 的异地交易模式，交易数: {}", userId, transactions.size());

        if (transactions.size() < 2) {
            return Optional.empty();
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(crossCityWindowMinutes);

        List<Transaction> recentTx = transactions.stream()
                .filter(tx -> tx.getEventTime().isAfter(windowStart))
                .sorted(Comparator.comparing(Transaction::getEventTime))
                .toList();

        if (recentTx.size() < 2) {
            return Optional.empty();
        }

        // 模拟 followedBy + IterativeCondition
        for (int i = 0; i < recentTx.size() - 1; i++) {
            Transaction first = recentTx.get(i);

            for (int j = i + 1; j < recentTx.size(); j++) {
                Transaction second = recentTx.get(j);

                // 检查是否异地
                if (!first.getCity().equals(second.getCity())) {
                    Duration interval = Duration.between(
                            first.getEventTime(), second.getEventTime());

                    if (interval.toMinutes() <= crossCityWindowMinutes) {
                        log.warn("【CEP 匹配成功】用户 {} 异地交易: {} → {}",
                                userId, first.getCity(), second.getCity());

                        Alert alert = new Alert();
                        alert.setAlertId(UUID.randomUUID().toString());
                        alert.setUserId(userId);
                        alert.setAlertType("CROSS_CITY");
                        alert.setLevel(Alert.AlertLevel.HIGH);
                        alert.setPatternName("CrossCityPattern");
                        alert.setMessage(String.format(
                                "用户 %s 在 %d 分钟内从 %s 切换到 %s 进行交易，疑似盗刷",
                                userId, interval.toMinutes(), first.getCity(), second.getCity()));
                        alert.setWindowStart(first.getEventTime());
                        alert.setWindowEnd(second.getEventTime());
                        alert.setRiskScore(40);
                        alert.setMatchedEventIds(List.of(
                                first.getTransactionId(), second.getTransactionId()));

                        return Optional.of(alert);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * 模式3: 大额交易检测
     * 
     * 【Flink CEP 实现】
     * Pattern.<Transaction>begin("high")
     * .where(tx -> tx.getAmount().compareTo(threshold) > 0)
     * .times(3)
     * .within(Time.minutes(30))
     */
    public Optional<Alert> matchHighAmountPattern(String userId, List<Transaction> transactions) {
        log.debug("检查用户 {} 的大额交易模式，交易数: {}", userId, transactions.size());

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(highAmountWindowMinutes);

        // 筛选大额交易
        List<Transaction> highAmountTx = transactions.stream()
                .filter(tx -> tx.getEventTime().isAfter(windowStart))
                .filter(tx -> tx.getAmount().compareTo(highAmountThreshold) > 0)
                .sorted(Comparator.comparing(Transaction::getEventTime))
                .toList();

        // 模拟 times(n)
        if (highAmountTx.size() >= highAmountCount) {
            BigDecimal totalAmount = highAmountTx.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.warn("【CEP 匹配成功】用户 {} 在 {}分钟内有 {} 笔大额交易，总金额: {}",
                    userId, highAmountWindowMinutes, highAmountTx.size(), totalAmount);

            Alert alert = new Alert();
            alert.setAlertId(UUID.randomUUID().toString());
            alert.setUserId(userId);
            alert.setAlertType("HIGH_AMOUNT");
            alert.setLevel(Alert.AlertLevel.CRITICAL);
            alert.setPatternName("HighAmountPattern");
            alert.setMessage(String.format(
                    "用户 %s 在 %d 分钟内完成 %d 笔大额交易（单笔超过 %s），总金额 %s",
                    userId, highAmountWindowMinutes, highAmountTx.size(),
                    highAmountThreshold, totalAmount));
            alert.setWindowStart(highAmountTx.get(0).getEventTime());
            alert.setWindowEnd(highAmountTx.get(highAmountTx.size() - 1).getEventTime());
            alert.setRiskScore(50);
            alert.setMatchedEventIds(
                    highAmountTx.stream().map(Transaction::getTransactionId).toList());
            alert.setDetails("{\"totalAmount\": " + totalAmount +
                    ", \"transactionCount\": " + highAmountTx.size() + "}");

            return Optional.of(alert);
        }

        return Optional.empty();
    }

    /**
     * 执行所有模式匹配
     */
    public List<Alert> matchAllPatterns(String userId,
            List<Transaction> transactions,
            List<LoginEvent> loginEvents) {
        List<Alert> alerts = new ArrayList<>();

        // 匹配登录失败模式
        matchLoginFailurePattern(userId, loginEvents).ifPresent(alerts::add);

        // 匹配异地交易模式
        matchCrossCityPattern(userId, transactions).ifPresent(alerts::add);

        // 匹配大额交易模式
        matchHighAmountPattern(userId, transactions).ifPresent(alerts::add);

        return alerts;
    }
}
