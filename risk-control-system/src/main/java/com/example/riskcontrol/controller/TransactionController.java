package com.example.riskcontrol.controller;

import com.example.riskcontrol.model.LoginEvent;
import com.example.riskcontrol.model.Transaction;
import com.example.riskcontrol.model.UserRiskProfile;
import com.example.riskcontrol.processor.StateManager;
import com.example.riskcontrol.processor.WindowAggregator;
import com.example.riskcontrol.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 交易控制器
 */
@RestController
@RequestMapping("/api/transaction")
@Tag(name = "交易", description = "交易风控 API")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StateManager stateManager;

    @Autowired
    private WindowAggregator windowAggregator;

    @PostMapping
    @Operation(summary = "提交交易", description = "提交交易进行风控检测")
    public ResponseEntity<TransactionService.TransactionResult> processTransaction(
            @RequestBody Transaction transaction) {

        if (transaction.getEventTime() == null) {
            transaction.setEventTime(LocalDateTime.now());
        }
        if (transaction.getTransactionId() == null) {
            transaction.setTransactionId("TX-" + System.currentTimeMillis());
        }

        TransactionService.TransactionResult result = transactionService.processTransaction(transaction);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量交易", description = "批量提交交易进行风控检测")
    public ResponseEntity<Map<String, Object>> processBatch(
            @RequestBody List<Transaction> transactions) {

        List<TransactionService.TransactionResult> results = new ArrayList<>();
        int blocked = 0;
        int alertCount = 0;

        for (Transaction tx : transactions) {
            if (tx.getEventTime() == null) {
                tx.setEventTime(LocalDateTime.now());
            }
            if (tx.getTransactionId() == null) {
                tx.setTransactionId("TX-" + System.currentTimeMillis() + "-" +
                        System.nanoTime() % 1000);
            }

            TransactionService.TransactionResult result = transactionService.processTransaction(tx);
            results.add(result);

            if (result.isBlocked())
                blocked++;
            alertCount += result.getAlerts().size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("total", transactions.size());
        response.put("blocked", blocked);
        response.put("alertCount", alertCount);
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "登录事件", description = "处理登录事件进行风控检测")
    public ResponseEntity<TransactionService.LoginResult> processLogin(
            @RequestBody LoginEvent event) {

        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
        }
        if (event.getEventId() == null) {
            event.setEventId("LOGIN-" + System.currentTimeMillis());
        }

        TransactionService.LoginResult result = transactionService.processLoginEvent(event);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate")
    @Operation(summary = "生成测试数据", description = "生成测试交易数据")
    public ResponseEntity<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "10") int count) {

        List<Transaction> transactions = transactionService.generateTestTransactions(count);

        Map<String, Object> response = new HashMap<>();
        response.put("generated", transactions.size());
        response.put("transactions", transactions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "用户风险画像", description = "获取用户风险画像")
    public ResponseEntity<UserRiskProfile> getUserProfile(@PathVariable String userId) {
        UserRiskProfile profile = stateManager.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "交易历史", description = "获取用户交易历史")
    public ResponseEntity<Map<String, Object>> getTransactionHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<Transaction> history = stateManager.getRecentTransactions(userId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("count", history.size());
        response.put("transactions", history);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/window")
    @Operation(summary = "窗口统计", description = "获取窗口聚合统计")
    public ResponseEntity<Map<String, Object>> getWindowStats() {
        return ResponseEntity.ok(windowAggregator.getAllWindowStats());
    }

    @GetMapping("/stats/state")
    @Operation(summary = "状态统计", description = "获取状态管理统计")
    public ResponseEntity<Map<String, Object>> getStateStats() {
        return ResponseEntity.ok(stateManager.getStateStats());
    }
}
