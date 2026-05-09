package com.example.riskcontrol.controller;

import com.example.riskcontrol.engine.RuleEngine;
import com.example.riskcontrol.model.RiskRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 规则控制器
 */
@RestController
@RequestMapping("/api/rules")
@Tag(name = "规则", description = "风控规则管理 API")
public class RuleController {

    @Autowired
    private RuleEngine ruleEngine;

    @GetMapping
    @Operation(summary = "获取所有规则")
    public ResponseEntity<List<RiskRule>> getAllRules() {
        return ResponseEntity.ok(ruleEngine.getAllRules());
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取启用的规则")
    public ResponseEntity<List<RiskRule>> getEnabledRules() {
        return ResponseEntity.ok(ruleEngine.getEnabledRules());
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "获取规则详情")
    public ResponseEntity<RiskRule> getRule(@PathVariable String ruleId) {
        return ruleEngine.getRule(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "添加规则", description = "添加新的风控规则（模拟 Broadcast State 广播）")
    public ResponseEntity<RiskRule> addRule(@RequestBody RiskRule rule) {
        if (rule.getRuleId() == null) {
            rule.setRuleId("RULE_" + UUID.randomUUID().toString().substring(0, 8));
        }
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());

        ruleEngine.addRule(rule);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "更新规则")
    public ResponseEntity<RiskRule> updateRule(
            @PathVariable String ruleId,
            @RequestBody RiskRule rule) {

        if (!ruleEngine.getRule(ruleId).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        rule.setRuleId(ruleId);
        rule.setUpdateTime(LocalDateTime.now());
        ruleEngine.updateRule(rule);

        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/{ruleId}")
    @Operation(summary = "删除规则")
    public ResponseEntity<Void> deleteRule(@PathVariable String ruleId) {
        ruleEngine.removeRule(ruleId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{ruleId}/toggle")
    @Operation(summary = "启用/禁用规则")
    public ResponseEntity<RiskRule> toggleRule(@PathVariable String ruleId) {
        return ruleEngine.getRule(ruleId)
                .map(rule -> {
                    rule.setEnabled(!rule.isEnabled());
                    rule.setUpdateTime(LocalDateTime.now());
                    ruleEngine.updateRule(rule);
                    return ResponseEntity.ok(rule);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
