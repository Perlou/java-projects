package com.example.riskcontrol.processor;

import com.example.riskcontrol.model.LoginEvent;
import com.example.riskcontrol.model.Transaction;
import com.example.riskcontrol.model.UserRiskProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 状态管理器
 * 
 * 【Flink 概念对应】
 * 模拟 Flink 的 Keyed State 机制
 * 
 * 在 Flink 中：
 * - ValueState<T>: 存储单值
 * - ListState<T>: 存储列表
 * - MapState<K,V>: 存储键值对
 * - ReducingState<T>: 聚合值
 * 
 * 这里使用 ConcurrentHashMap 模拟每个用户（Key）的独立状态
 */
@Component
public class StateManager {

    private static final Logger log = LoggerFactory.getLogger(StateManager.class);

    /**
     * 用户风险画像状态
     * 模拟 ValueState<UserRiskProfile>，keyBy userId
     */
    private final Map<String, UserRiskProfile> userProfileState = new ConcurrentHashMap<>();

    /**
     * 用户交易历史
     * 模拟 ListState<Transaction>，keyBy userId
     */
    private final Map<String, LinkedList<Transaction>> userTransactionHistory = new ConcurrentHashMap<>();

    /**
     * 用户登录事件历史
     * 模拟 ListState<LoginEvent>，keyBy userId
     */
    private final Map<String, LinkedList<LoginEvent>> userLoginHistory = new ConcurrentHashMap<>();

    /**
     * 状态配置
     */
    private static final int MAX_HISTORY_SIZE = 100; // 每个用户最多保留100条历史
    private static final int LOGIN_HISTORY_SIZE = 20; // 登录历史保留20条

    @PostConstruct
    public void init() {
        log.info("StateManager 初始化完成");
        log.info("【Flink 知识点】模拟 Keyed State:");
        log.info("  • userProfileState → ValueState<UserRiskProfile>");
        log.info("  • userTransactionHistory → ListState<Transaction>");
        log.info("  • userLoginHistory → ListState<LoginEvent>");
    }

    // ==================== ValueState 模拟 ====================

    /**
     * 获取用户风险画像 (模拟 ValueState.value())
     */
    public UserRiskProfile getUserProfile(String userId) {
        return userProfileState.computeIfAbsent(userId, UserRiskProfile::new);
    }

    /**
     * 更新用户风险画像 (模拟 ValueState.update())
     */
    public void updateUserProfile(String userId, UserRiskProfile profile) {
        userProfileState.put(userId, profile);
        log.debug("【State Update】用户 {} 风险画像已更新，当前分数: {}",
                userId, profile.getRiskScore());
    }

    // ==================== ListState 模拟 ====================

    /**
     * 添加交易到历史 (模拟 ListState.add())
     */
    public void addTransaction(String userId, Transaction tx) {
        LinkedList<Transaction> history = userTransactionHistory
                .computeIfAbsent(userId, k -> new LinkedList<>());

        history.addLast(tx);

        // 保持最大容量（模拟状态 TTL 的简化版）
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeFirst();
        }

        log.debug("【ListState.add()】用户 {} 交易历史新增，当前数量: {}",
                userId, history.size());
    }

    /**
     * 获取用户交易历史 (模拟 ListState.get())
     */
    public List<Transaction> getTransactionHistory(String userId) {
        return userTransactionHistory.getOrDefault(userId, new LinkedList<>());
    }

    /**
     * 获取最近N笔交易
     */
    public List<Transaction> getRecentTransactions(String userId, int n) {
        LinkedList<Transaction> history = userTransactionHistory
                .getOrDefault(userId, new LinkedList<>());

        int start = Math.max(0, history.size() - n);
        return new ArrayList<>(history.subList(start, history.size()));
    }

    /**
     * 添加登录事件到历史
     */
    public void addLoginEvent(String userId, LoginEvent event) {
        LinkedList<LoginEvent> history = userLoginHistory
                .computeIfAbsent(userId, k -> new LinkedList<>());

        history.addLast(event);

        while (history.size() > LOGIN_HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    /**
     * 获取登录事件历史
     */
    public List<LoginEvent> getLoginHistory(String userId) {
        return userLoginHistory.getOrDefault(userId, new LinkedList<>());
    }

    /**
     * 获取最近的登录事件
     */
    public List<LoginEvent> getRecentLoginEvents(String userId, int n) {
        LinkedList<LoginEvent> history = userLoginHistory
                .getOrDefault(userId, new LinkedList<>());

        int start = Math.max(0, history.size() - n);
        return new ArrayList<>(history.subList(start, history.size()));
    }

    // ==================== 状态清理 ====================

    /**
     * 清除用户状态 (模拟 State.clear())
     */
    public void clearUserState(String userId) {
        userProfileState.remove(userId);
        userTransactionHistory.remove(userId);
        userLoginHistory.remove(userId);
        log.info("【State Clear】用户 {} 状态已清除", userId);
    }

    /**
     * 获取状态统计
     */
    public Map<String, Object> getStateStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userProfileState.size());
        stats.put("totalTransactionRecords",
                userTransactionHistory.values().stream().mapToInt(List::size).sum());
        stats.put("totalLoginRecords",
                userLoginHistory.values().stream().mapToInt(List::size).sum());
        return stats;
    }

    /**
     * 获取所有用户画像
     */
    public Collection<UserRiskProfile> getAllProfiles() {
        return userProfileState.values();
    }
}
