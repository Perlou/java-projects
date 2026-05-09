package com.example.seckill.controller;

import com.example.seckill.common.Result;
import com.example.seckill.security.auth.JwtTokenService;
import com.example.seckill.security.auth.OAuth2DemoService;
import com.example.seckill.security.coding.SecureCodingService;
import com.example.seckill.security.rbac.RBACService;
import com.example.seckill.security.zerotrust.ZeroTrustService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 19: 安全架构演示控制器
 * 
 * 提供 OAuth2、RBAC、零信任、安全编码的演示 API
 */
@RestController
@RequestMapping("/api/demo/security")
@Tag(name = "安全架构演示", description = "Phase 19 - OAuth2/JWT、RBAC/ABAC、零信任、安全编码")
public class SecurityDemoController {

    private final JwtTokenService jwtTokenService;
    private final OAuth2DemoService oauth2DemoService;
    private final RBACService rbacService;
    private final ZeroTrustService zeroTrustService;
    private final SecureCodingService secureCodingService;

    public SecurityDemoController(JwtTokenService jwtTokenService,
            OAuth2DemoService oauth2DemoService,
            RBACService rbacService,
            ZeroTrustService zeroTrustService,
            SecureCodingService secureCodingService) {
        this.jwtTokenService = jwtTokenService;
        this.oauth2DemoService = oauth2DemoService;
        this.rbacService = rbacService;
        this.zeroTrustService = zeroTrustService;
        this.secureCodingService = secureCodingService;
    }

    // ==================== JWT Token ====================

    @PostMapping("/jwt/generate")
    @Operation(summary = "生成 JWT Token", description = "演示 Access Token 和 Refresh Token 生成")
    public Result<Map<String, Object>> generateToken(
            @RequestParam Long userId,
            @RequestParam String username,
            @RequestParam(defaultValue = "USER") String role) {

        String accessToken = jwtTokenService.generateAccessToken(userId, username, List.of(role));
        String refreshToken = jwtTokenService.generateRefreshToken(userId);

        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");

        return Result.success("Token 已生成", tokens);
    }

    @PostMapping("/jwt/validate")
    @Operation(summary = "验证 JWT Token")
    public Result<Map<String, Object>> validateToken(@RequestParam String token) {
        Map<String, Object> result = new LinkedHashMap<>();

        boolean valid = jwtTokenService.validateToken(token);
        result.put("valid", valid);

        if (valid) {
            result.put("userId", jwtTokenService.getUserIdFromToken(token));
            result.put("roles", jwtTokenService.getRolesFromToken(token));
        }

        return Result.success("验证结果", result);
    }

    @GetMapping("/jwt/info")
    @Operation(summary = "JWT Token 信息说明")
    public Result<Map<String, Object>> getTokenInfo() {
        return Result.success("Token 信息", jwtTokenService.getTokenInfo());
    }

    // ==================== OAuth2 ====================

    @GetMapping("/oauth2/grant-types")
    @Operation(summary = "OAuth2 授权模式说明")
    public Result<Map<String, Object>> getGrantTypes() {
        return Result.success("OAuth2 授权模式", oauth2DemoService.getGrantTypesInfo());
    }

    @GetMapping("/oauth2/roles")
    @Operation(summary = "OAuth2 角色说明")
    public Result<Map<String, String>> getOAuth2Roles() {
        return Result.success("OAuth2 角色", oauth2DemoService.getRolesInfo());
    }

    @PostMapping("/oauth2/pkce/generate")
    @Operation(summary = "生成 PKCE 参数", description = "用于公共客户端（SPA/移动App）的安全扩展")
    public Result<Map<String, String>> generatePKCE() {
        String codeVerifier = oauth2DemoService.generateCodeVerifier();
        String codeChallenge = oauth2DemoService.generateCodeChallenge(codeVerifier);

        Map<String, String> pkce = new LinkedHashMap<>();
        pkce.put("codeVerifier", codeVerifier);
        pkce.put("codeChallenge", codeChallenge);
        pkce.put("codeChallengeMethod", "S256");

        return Result.success("PKCE 参数", pkce);
    }

    // ==================== RBAC ====================

    @GetMapping("/rbac/roles")
    @Operation(summary = "获取所有角色定义")
    public Result<?> getAllRoles() {
        return Result.success("角色列表", rbacService.getAllRoles());
    }

    @GetMapping("/rbac/models")
    @Operation(summary = "RBAC 模型层级说明")
    public Result<Map<String, Object>> getRBACModels() {
        return Result.success("RBAC 模型", rbacService.getRBACModelsInfo());
    }

    @PostMapping("/rbac/assign")
    @Operation(summary = "为用户分配角色")
    public Result<String> assignRole(@RequestParam Long userId, @RequestParam String role) {
        rbacService.assignRole(userId, role);
        return Result.success("角色已分配: " + role + " -> 用户 " + userId, null);
    }

    @GetMapping("/rbac/check")
    @Operation(summary = "检查用户权限")
    public Result<Map<String, Object>> checkPermission(
            @RequestParam Long userId,
            @RequestParam String resource,
            @RequestParam String action) {

        boolean hasPermission = rbacService.hasPermission(userId, resource, action);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("permission", resource + ":" + action);
        result.put("allowed", hasPermission);
        result.put("userPermissions", rbacService.getUserPermissions(userId).stream()
                .map(p -> p.resource + ":" + p.action).toList());

        return Result.success("权限检查结果", result);
    }

    @GetMapping("/rbac/naming")
    @Operation(summary = "权限命名规范")
    public Result<Map<String, Object>> getPermissionNaming() {
        return Result.success("权限命名规范", rbacService.getPermissionNamingConvention());
    }

    // ==================== 零信任 ====================

    @GetMapping("/zerotrust/principles")
    @Operation(summary = "零信任核心原则")
    public Result<Map<String, Object>> getZeroTrustPrinciples() {
        return Result.success("零信任原则", zeroTrustService.getCorePrinciples());
    }

    @GetMapping("/zerotrust/components")
    @Operation(summary = "零信任架构组件")
    public Result<Map<String, String>> getZeroTrustComponents() {
        return Result.success("架构组件", zeroTrustService.getArchitectureComponents());
    }

    @GetMapping("/zerotrust/phases")
    @Operation(summary = "零信任实施阶段")
    public Result<?> getImplementationPhases() {
        return Result.success("实施阶段", zeroTrustService.getImplementationPhases());
    }

    @PostMapping("/zerotrust/assess")
    @Operation(summary = "风险评估演示", description = "评估访问请求的风险等级")
    public Result<?> assessRisk(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false, defaultValue = "127.0.0.1") String ipAddress,
            @RequestParam(defaultValue = "true") boolean hasValidToken,
            @RequestParam(defaultValue = "false") boolean sensitiveOperation) {

        ZeroTrustService.AccessRequest request = new ZeroTrustService.AccessRequest();
        request.userId = userId;
        request.deviceId = deviceId;
        request.ipAddress = ipAddress;
        request.hasValidToken = hasValidToken;
        request.sensitiveOperation = sensitiveOperation;
        request.isAnomalous = false;

        ZeroTrustService.RiskAssessment assessment = zeroTrustService.assessRisk(request);

        return Result.success("风险评估结果", assessment);
    }

    // ==================== 安全编码 ====================

    @GetMapping("/coding/owasp")
    @Operation(summary = "OWASP Top 10 (2021)")
    public Result<?> getOWASPTop10() {
        return Result.success("OWASP Top 10", secureCodingService.getOWASPTop10());
    }

    @PostMapping("/coding/xss-encode")
    @Operation(summary = "XSS 编码演示", description = "展示如何对恶意输入进行编码")
    public Result<Map<String, Object>> demonstrateXSSEncoding(
            @RequestParam(defaultValue = "<script>alert('XSS')</script>") String input) {
        return Result.success("XSS 编码结果", secureCodingService.demonstrateXSSEncoding(input));
    }

    @GetMapping("/coding/headers")
    @Operation(summary = "推荐的安全响应头")
    public Result<Map<String, Object>> getSecurityHeaders() {
        return Result.success("安全响应头", secureCodingService.getSecurityHeaders());
    }

    @GetMapping("/coding/principles")
    @Operation(summary = "安全编码核心原则")
    public Result<List<String>> getSecureCodingPrinciples() {
        return Result.success("安全编码原则", secureCodingService.getSecureCodingPrinciples());
    }

    @PostMapping("/coding/validate")
    @Operation(summary = "输入验证演示")
    public Result<Map<String, Object>> validateInput(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {

        Map<String, Object> results = new LinkedHashMap<>();

        if (username != null) {
            results.put("username", secureCodingService.validateUsername(username));
        }
        if (email != null) {
            results.put("email", secureCodingService.validateEmail(email));
        }

        return Result.success("验证结果", results);
    }

    // ==================== 综合概览 ====================

    @GetMapping("/overview")
    @Operation(summary = "安全架构概览")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new LinkedHashMap<>();

        overview.put("modules", Map.of(
                "OAuth2/JWT", "认证与授权，Token 管理",
                "RBAC/ABAC", "基于角色和属性的访问控制",
                "零信任", "永不信任，始终验证",
                "安全编码", "OWASP Top 10 防御"));

        overview.put("rbacRoles", rbacService.getAllRoles().size());
        overview.put("owasp", secureCodingService.getOWASPTop10().size() + " 项风险");

        return Result.success("安全架构概览", overview);
    }
}
