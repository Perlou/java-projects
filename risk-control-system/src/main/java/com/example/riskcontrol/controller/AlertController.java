package com.example.riskcontrol.controller;

import com.example.riskcontrol.model.Alert;
import com.example.riskcontrol.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 告警控制器
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "告警", description = "风控告警 API")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    @Operation(summary = "获取所有告警")
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "获取告警详情")
    public ResponseEntity<Alert> getAlert(@PathVariable String alertId) {
        return alertService.getAlert(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户告警")
    public ResponseEntity<List<Alert>> getAlertsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(alertService.getAlertsByUser(userId));
    }

    @GetMapping("/type/{alertType}")
    @Operation(summary = "按类型获取告警")
    public ResponseEntity<List<Alert>> getAlertsByType(@PathVariable String alertType) {
        return ResponseEntity.ok(alertService.getAlertsByType(alertType));
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "按级别获取告警")
    public ResponseEntity<List<Alert>> getAlertsByLevel(@PathVariable Alert.AlertLevel level) {
        return ResponseEntity.ok(alertService.getAlertsByLevel(level));
    }

    @GetMapping("/recent")
    @Operation(summary = "获取最近告警")
    public ResponseEntity<List<Alert>> getRecentAlerts(
            @RequestParam(defaultValue = "1") int hours) {
        return ResponseEntity.ok(alertService.getRecentAlerts(hours));
    }

    @GetMapping("/stats")
    @Operation(summary = "告警统计")
    public ResponseEntity<Map<String, Object>> getAlertStats() {
        return ResponseEntity.ok(alertService.getAlertStats());
    }

    @PutMapping("/{alertId}/status")
    @Operation(summary = "更新告警状态")
    public ResponseEntity<Void> updateAlertStatus(
            @PathVariable String alertId,
            @RequestParam Alert.AlertStatus status) {
        alertService.updateAlertStatus(alertId, status);
        return ResponseEntity.ok().build();
    }
}
