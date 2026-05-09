package com.example.riskcontrol.controller;

import com.example.riskcontrol.engine.PatternMatcher;
import com.example.riskcontrol.model.Alert;
import com.example.riskcontrol.model.LoginEvent;
import com.example.riskcontrol.model.Transaction;
import com.example.riskcontrol.processor.StateManager;
import com.example.riskcontrol.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 演示控制器
 * 
 * 用于演示 Flink CEP 等概念
 */
@RestController
@RequestMapping("/api/demo")
@Tag(name = "演示", description = "Flink 概念演示 API")
public class DemoController {

    @Autowired
    private PatternMatcher patternMatcher;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StateManager stateManager;

    @PostMapping("/cep/login-failure")
    @Operation(summary = "演示登录失败 CEP 模式", description = "模拟连续登录失败，触发 CEP 模式匹配")
    public ResponseEntity<Map<String, Object>> demoLoginFailureCep(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(defaultValue = "3") int failCount) {

        Map<String, Object> result = new HashMap<>();
        result.put("flinkConcept", "CEP - Pattern.begin().next().next().within()");
        result.put("description", "模拟 Flink CEP 连续登录失败检测");

        // 生成连续登录失败事件
        List<LoginEvent> events = new ArrayList<>();
        for (int i = 0; i < failCount; i++) {
            LoginEvent event = new LoginEvent();
            event.setEventId("LOGIN-DEMO-" + System.currentTimeMillis() + "-" + i);
            event.setUserId(userId);
            event.setStatus("FAIL");
            event.setCity("北京");
            event.setIpAddress("192.168.1." + i);
            event.setEventTime(LocalDateTime.now().minusMinutes(failCount - i));
            event.setFailReason("密码错误");

            events.add(event);
            stateManager.addLoginEvent(userId, event);
        }

        result.put("generatedEvents", events.size());

        // 执行模式匹配
        List<LoginEvent> history = stateManager.getLoginHistory(userId);
        Optional<Alert> alert = patternMatcher.matchLoginFailurePattern(userId, history);

        if (alert.isPresent()) {
            result.put("matched", true);
            result.put("alert", alert.get());
            result.put("message", "CEP 模式匹配成功！检测到连续登录失败");
        } else {
            result.put("matched", false);
            result.put("message", "未触发模式匹配（可能失败次数不足或时间窗口不满足）");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cep/cross-city")
    @Operation(summary = "演示异地交易 CEP 模式", description = "模拟短时间内异地交易，触发 CEP 模式匹配")
    public ResponseEntity<Map<String, Object>> demoCrossCityCep(
            @RequestParam(defaultValue = "user002") String userId) {

        Map<String, Object> result = new HashMap<>();
        result.put("flinkConcept", "CEP - Pattern.begin().followedBy().where(IterativeCondition)");
        result.put("description", "模拟 Flink CEP 异地交易检测");

        // 生成两笔不同城市的交易
        String[] cities = { "北京", "上海" };
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Transaction tx = new Transaction();
            tx.setTransactionId("TX-DEMO-" + System.currentTimeMillis() + "-" + i);
            tx.setUserId(userId);
            tx.setAmount(BigDecimal.valueOf(1000 + i * 500));
            tx.setCity(cities[i]);
            tx.setCountry("CN");
            tx.setCurrency("CNY");
            tx.setEventTime(LocalDateTime.now().minusMinutes(5 - i * 3));
            tx.setTransactionType("PAYMENT");
            tx.setChannel("APP");

            transactions.add(tx);
            stateManager.addTransaction(userId, tx);
        }

        result.put("generatedTransactions", transactions);

        // 执行模式匹配
        List<Transaction> history = stateManager.getTransactionHistory(userId);
        Optional<Alert> alert = patternMatcher.matchCrossCityPattern(userId, history);

        if (alert.isPresent()) {
            result.put("matched", true);
            result.put("alert", alert.get());
            result.put("message", "CEP 模式匹配成功！检测到异地交易");
        } else {
            result.put("matched", false);
            result.put("message", "未触发模式匹配");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cep/high-amount")
    @Operation(summary = "演示大额交易 CEP 模式", description = "模拟连续大额交易，触发 CEP 模式匹配")
    public ResponseEntity<Map<String, Object>> demoHighAmountCep(
            @RequestParam(defaultValue = "user003") String userId,
            @RequestParam(defaultValue = "60000") BigDecimal amount,
            @RequestParam(defaultValue = "3") int count) {

        Map<String, Object> result = new HashMap<>();
        result.put("flinkConcept", "CEP - Pattern.begin().times(n).where()");
        result.put("description", "模拟 Flink CEP 大额交易检测");

        // 生成多笔大额交易
        List<Transaction> transactions = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            Transaction tx = new Transaction();
            tx.setTransactionId("TX-DEMO-HIGH-" + System.currentTimeMillis() + "-" + i);
            tx.setUserId(userId);
            tx.setAmount(amount.add(BigDecimal.valueOf(random.nextInt(10000))));
            tx.setCity("深圳");
            tx.setCountry("CN");
            tx.setCurrency("CNY");
            tx.setEventTime(LocalDateTime.now().minusMinutes(count - i));
            tx.setTransactionType("TRANSFER");
            tx.setChannel("APP");

            transactions.add(tx);
            stateManager.addTransaction(userId, tx);
        }

        result.put("generatedTransactions", transactions);

        // 执行模式匹配
        List<Transaction> history = stateManager.getTransactionHistory(userId);
        Optional<Alert> alert = patternMatcher.matchHighAmountPattern(userId, history);

        if (alert.isPresent()) {
            result.put("matched", true);
            result.put("alert", alert.get());
            result.put("message", "CEP 模式匹配成功！检测到连续大额交易");
        } else {
            result.put("matched", false);
            result.put("message", "未触发模式匹配（金额或次数可能不足）");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/flink-concepts")
    @Operation(summary = "Flink 概念对照", description = "展示项目中模拟的 Flink 概念")
    public ResponseEntity<Map<String, Object>> getFlinkConcepts() {
        Map<String, Object> concepts = new LinkedHashMap<>();

        concepts.put("1_StateManager", Map.of(
                "flinkConcept", "Keyed State (ValueState, ListState, MapState)",
                "description", "每个用户（Key）有独立的状态存储，包括风险画像、交易历史、登录历史",
                "implementation", "ConcurrentHashMap 模拟 State Backend"));

        concepts.put("2_PatternMatcher", Map.of(
                "flinkConcept", "CEP (Complex Event Processing)",
                "description", "复杂事件模式匹配，检测登录失败、异地交易、大额交易等模式",
                "patterns", List.of(
                        "LoginFailurePattern: begin().next().next().within()",
                        "CrossCityPattern: begin().followedBy().where(IterativeCondition)",
                        "HighAmountPattern: begin().times(n).where()")));

        concepts.put("3_RuleEngine", Map.of(
                "flinkConcept", "Broadcast State",
                "description", "规则动态广播到所有处理实例，支持实时规则更新",
                "implementation", "ConcurrentHashMap 模拟广播状态"));

        concepts.put("4_WindowAggregator", Map.of(
                "flinkConcept", "Window (Tumbling, Sliding)",
                "description", "时间窗口聚合统计，计算交易次数、金额等指标",
                "windowTypes", List.of(
                        "TumblingWindow: 滚动窗口，不重叠",
                        "SlidingWindow: 滑动窗口，可重叠")));

        concepts.put("5_TransactionService", Map.of(
                "flinkConcept", "DataStream Processing Pipeline",
                "description", "数据流处理管线：接收事件 → 更新状态 → 窗口聚合 → 规则匹配 → CEP → 告警",
                "flow", "Source → State → Window → Broadcast Join → CEP → Sink"));

        return ResponseEntity.ok(concepts);
    }
}
