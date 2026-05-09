package com.example.riskcontrol.service;

import com.example.riskcontrol.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 告警服务
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final Map<String, Alert> alertStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("AlertService 初始化完成");
    }

    public void addAlert(Alert alert) {
        alertStore.put(alert.getAlertId(), alert);
        log.warn("新增告警: [{}] {} - {}",
                alert.getLevel(), alert.getAlertType(), alert.getMessage());
    }

    public Optional<Alert> getAlert(String alertId) {
        return Optional.ofNullable(alertStore.get(alertId));
    }

    public List<Alert> getAllAlerts() {
        return alertStore.values().stream()
                .sorted(Comparator.comparing(Alert::getTriggerTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> getAlertsByUser(String userId) {
        return alertStore.values().stream()
                .filter(a -> userId.equals(a.getUserId()))
                .sorted(Comparator.comparing(Alert::getTriggerTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> getAlertsByType(String alertType) {
        return alertStore.values().stream()
                .filter(a -> alertType.equals(a.getAlertType()))
                .sorted(Comparator.comparing(Alert::getTriggerTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> getAlertsByLevel(Alert.AlertLevel level) {
        return alertStore.values().stream()
                .filter(a -> level.equals(a.getLevel()))
                .sorted(Comparator.comparing(Alert::getTriggerTime).reversed())
                .collect(Collectors.toList());
    }

    public List<Alert> getRecentAlerts(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return alertStore.values().stream()
                .filter(a -> a.getTriggerTime().isAfter(since))
                .sorted(Comparator.comparing(Alert::getTriggerTime).reversed())
                .collect(Collectors.toList());
    }

    public void updateAlertStatus(String alertId, Alert.AlertStatus status) {
        Alert alert = alertStore.get(alertId);
        if (alert != null) {
            alert.setStatus(status);
            log.info("告警 {} 状态已更新为: {}", alertId, status);
        }
    }

    public Map<String, Object> getAlertStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", alertStore.size());

        // 按级别统计
        Map<Alert.AlertLevel, Long> byLevel = alertStore.values().stream()
                .collect(Collectors.groupingBy(Alert::getLevel, Collectors.counting()));
        stats.put("byLevel", byLevel);

        // 按类型统计
        Map<String, Long> byType = alertStore.values().stream()
                .collect(Collectors.groupingBy(Alert::getAlertType, Collectors.counting()));
        stats.put("byType", byType);

        // 按状态统计
        Map<Alert.AlertStatus, Long> byStatus = alertStore.values().stream()
                .collect(Collectors.groupingBy(Alert::getStatus, Collectors.counting()));
        stats.put("byStatus", byStatus);

        // 最近1小时告警数
        long recentCount = alertStore.values().stream()
                .filter(a -> a.getTriggerTime().isAfter(LocalDateTime.now().minusHours(1)))
                .count();
        stats.put("lastHour", recentCount);

        return stats;
    }
}
