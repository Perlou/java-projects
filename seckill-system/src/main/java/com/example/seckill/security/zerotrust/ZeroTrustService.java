package com.example.seckill.security.zerotrust;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Phase 19: 零信任架构服务
 * 
 * 实现零信任核心概念的演示
 */
@Service
public class ZeroTrustService {

    // 模拟设备注册
    private final Map<String, DeviceInfo> registeredDevices = new HashMap<>();

    // 风险评分阈值
    private static final int RISK_THRESHOLD_LOW = 30;
    private static final int RISK_THRESHOLD_HIGH = 70;

    // ==================== 上下文验证 ====================

    /**
     * 评估访问请求风险
     */
    public RiskAssessment assessRisk(AccessRequest request) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.factors = new ArrayList<>();
        int totalScore = 0;

        // 1. 身份验证因素
        if (request.hasValidToken) {
            assessment.factors.add(new RiskFactor("身份验证", "Token 有效", 0));
        } else {
            assessment.factors.add(new RiskFactor("身份验证", "Token 无效或过期", 40));
            totalScore += 40;
        }

        // 2. 设备因素
        if (request.deviceId != null && registeredDevices.containsKey(request.deviceId)) {
            DeviceInfo device = registeredDevices.get(request.deviceId);
            if (device.isCompliant) {
                assessment.factors.add(new RiskFactor("设备状态", "已注册且合规", 0));
            } else {
                assessment.factors.add(new RiskFactor("设备状态", "已注册但不合规", 20));
                totalScore += 20;
            }
        } else {
            assessment.factors.add(new RiskFactor("设备状态", "未知设备", 30));
            totalScore += 30;
        }

        // 3. 地理位置因素
        if (request.ipAddress != null) {
            if (isInternalIP(request.ipAddress)) {
                assessment.factors.add(new RiskFactor("网络位置", "内网访问", 0));
            } else if (isKnownLocation(request.ipAddress)) {
                assessment.factors.add(new RiskFactor("网络位置", "已知位置", 10));
                totalScore += 10;
            } else {
                assessment.factors.add(new RiskFactor("网络位置", "未知位置", 25));
                totalScore += 25;
            }
        }

        // 4. 行为因素
        if (request.isAnomalous) {
            assessment.factors.add(new RiskFactor("行为分析", "检测到异常行为", 35));
            totalScore += 35;
        } else {
            assessment.factors.add(new RiskFactor("行为分析", "行为正常", 0));
        }

        // 5. 请求敏感度
        if (request.sensitiveOperation) {
            assessment.factors.add(new RiskFactor("操作类型", "敏感操作", 15));
            totalScore += 15;
        }

        assessment.totalScore = Math.min(100, totalScore);
        assessment.decision = determineDecision(assessment.totalScore);

        return assessment;
    }

    private AccessDecision determineDecision(int score) {
        if (score < RISK_THRESHOLD_LOW) {
            return AccessDecision.ALLOW;
        } else if (score < RISK_THRESHOLD_HIGH) {
            return AccessDecision.STEP_UP_AUTH; // 需要额外验证
        } else {
            return AccessDecision.DENY;
        }
    }

    // ==================== 设备管理 ====================

    /**
     * 注册设备
     */
    public void registerDevice(String deviceId, String deviceType, boolean isCompliant) {
        DeviceInfo device = new DeviceInfo();
        device.deviceId = deviceId;
        device.deviceType = deviceType;
        device.isCompliant = isCompliant;
        device.registeredAt = System.currentTimeMillis();
        registeredDevices.put(deviceId, device);
    }

    /**
     * 验证设备
     */
    public boolean isDeviceRegistered(String deviceId) {
        return registeredDevices.containsKey(deviceId);
    }

    // ==================== 辅助方法 ====================

    private boolean isInternalIP(String ip) {
        return ip.startsWith("10.") || ip.startsWith("192.168.") ||
                ip.startsWith("172.16.") || "127.0.0.1".equals(ip);
    }

    private boolean isKnownLocation(String ip) {
        // 模拟：假设某些 IP 段是已知的办公地点
        return ip.startsWith("203.") || ip.startsWith("114.");
    }

    // ==================== 零信任概念说明 ====================

    /**
     * 获取零信任核心原则
     */
    public Map<String, Object> getCorePrinciples() {
        Map<String, Object> principles = new LinkedHashMap<>();

        principles.put("Never Trust, Always Verify", Map.of(
                "description", "永不信任，始终验证",
                "practices", List.of(
                        "每次请求都需要身份验证",
                        "持续验证，而非一次登录持久信任",
                        "验证用户身份 + 设备状态 + 上下文")));

        principles.put("Least Privilege", Map.of(
                "description", "最小权限原则",
                "practices", List.of(
                        "仅授予完成任务所需的最小权限",
                        "Just-In-Time (JIT) 访问",
                        "定期审查和回收权限")));

        principles.put("Assume Breach", Map.of(
                "description", "假设已被入侵",
                "practices", List.of(
                        "设计时假设攻击者已在网络中",
                        "微分段，限制横向移动",
                        "端到端加密",
                        "全面监控和日志记录")));

        return principles;
    }

    /**
     * 获取零信任架构组件说明
     */
    public Map<String, String> getArchitectureComponents() {
        Map<String, String> components = new LinkedHashMap<>();
        components.put("身份提供者 (IdP)", "统一身份认证（Okta、Azure AD、Keycloak）+ MFA");
        components.put("设备管理 (MDM/UEM)", "设备注册、合规性检查、健康状态评估");
        components.put("策略引擎 (PDP)", "基于上下文的访问决策、动态权限策略");
        components.put("策略执行点 (PEP)", "API 网关、VPN、服务网格、数据库代理");
        components.put("微分段", "细粒度网络隔离、服务间 mTLS");
        return components;
    }

    /**
     * 获取实施阶段
     */
    public List<Map<String, Object>> getImplementationPhases() {
        List<Map<String, Object>> phases = new ArrayList<>();

        phases.add(Map.of(
                "phase", "阶段1: 身份基础",
                "tasks", List.of("统一身份提供者 (SSO)", "启用 MFA", "强密码策略", "权限最小化审查")));

        phases.add(Map.of(
                "phase", "阶段2: 设备信任",
                "tasks", List.of("设备注册和管理", "设备合规性检查", "证书认证")));

        phases.add(Map.of(
                "phase", "阶段3: 网络分段",
                "tasks", List.of("应用级别访问控制", "服务网格 (Istio/Linkerd)", "mTLS 服务间通信")));

        phases.add(Map.of(
                "phase", "阶段4: 持续监控",
                "tasks", List.of("SIEM 安全信息事件管理", "用户行为分析 (UEBA)", "自动响应和修复")));

        return phases;
    }

    // ==================== 内部类 ====================

    public static class AccessRequest {
        public Long userId;
        public String deviceId;
        public String ipAddress;
        public boolean hasValidToken;
        public boolean isAnomalous;
        public boolean sensitiveOperation;
    }

    public static class RiskAssessment {
        public int totalScore;
        public AccessDecision decision;
        public List<RiskFactor> factors;
    }

    public static class RiskFactor {
        public String category;
        public String detail;
        public int score;

        public RiskFactor(String category, String detail, int score) {
            this.category = category;
            this.detail = detail;
            this.score = score;
        }
    }

    public static class DeviceInfo {
        public String deviceId;
        public String deviceType;
        public boolean isCompliant;
        public long registeredAt;
    }

    public enum AccessDecision {
        ALLOW, // 允许访问
        STEP_UP_AUTH, // 需要额外验证
        DENY // 拒绝访问
    }
}
