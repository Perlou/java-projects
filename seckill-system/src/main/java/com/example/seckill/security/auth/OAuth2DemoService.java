package com.example.seckill.security.auth;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Phase 19: OAuth2 授权服务（演示）
 * 
 * 演示 OAuth2 核心概念和授权流程
 */
@Service
public class OAuth2DemoService {

    // 模拟客户端注册信息
    private final Map<String, ClientInfo> registeredClients = new HashMap<>();

    // 模拟授权码存储
    private final Map<String, AuthorizationCode> authCodes = new HashMap<>();

    public OAuth2DemoService() {
        // 注册演示客户端
        registeredClients.put("demo-app", new ClientInfo(
                "demo-app",
                "demo-secret",
                "http://localhost:3000/callback",
                List.of("read", "write")));
    }

    // ==================== 授权码模式 ====================

    /**
     * 生成授权码
     */
    public String generateAuthorizationCode(String clientId, Long userId,
            String redirectUri, String scope, String state) {
        ClientInfo client = registeredClients.get(clientId);
        if (client == null) {
            throw new IllegalArgumentException("未注册的客户端: " + clientId);
        }

        String code = UUID.randomUUID().toString().replace("-", "");

        AuthorizationCode authCode = new AuthorizationCode();
        authCode.code = code;
        authCode.clientId = clientId;
        authCode.userId = userId;
        authCode.redirectUri = redirectUri;
        authCode.scope = scope;
        authCode.state = state;
        authCode.createdAt = System.currentTimeMillis();
        authCode.expiresAt = authCode.createdAt + 10 * 60 * 1000; // 10 分钟

        authCodes.put(code, authCode);
        return code;
    }

    /**
     * 交换授权码为 Token
     */
    public Map<String, Object> exchangeCodeForToken(String code, String clientId,
            String clientSecret, String redirectUri) {
        AuthorizationCode authCode = authCodes.get(code);

        if (authCode == null) {
            throw new IllegalArgumentException("无效的授权码");
        }

        if (System.currentTimeMillis() > authCode.expiresAt) {
            authCodes.remove(code);
            throw new IllegalArgumentException("授权码已过期");
        }

        ClientInfo client = registeredClients.get(clientId);
        if (client == null || !client.secret.equals(clientSecret)) {
            throw new IllegalArgumentException("客户端认证失败");
        }

        if (!authCode.redirectUri.equals(redirectUri)) {
            throw new IllegalArgumentException("redirect_uri 不匹配");
        }

        // 授权码只能使用一次
        authCodes.remove(code);

        // 生成 Token（简化版）
        Map<String, Object> tokens = new LinkedHashMap<>();
        tokens.put("access_token", "at_" + UUID.randomUUID().toString().replace("-", ""));
        tokens.put("refresh_token", "rt_" + UUID.randomUUID().toString().replace("-", ""));
        tokens.put("token_type", "Bearer");
        tokens.put("expires_in", 900); // 15 分钟
        tokens.put("scope", authCode.scope);

        return tokens;
    }

    // ==================== PKCE 扩展 ====================

    /**
     * 生成 PKCE Code Verifier
     */
    public String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 计算 Code Challenge
     */
    public String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * 验证 PKCE
     */
    public boolean verifyPKCE(String codeVerifier, String codeChallenge) {
        String computed = generateCodeChallenge(codeVerifier);
        return computed.equals(codeChallenge);
    }

    // ==================== OAuth2 概念说明 ====================

    /**
     * 获取 OAuth2 授权模式说明
     */
    public Map<String, Object> getGrantTypesInfo() {
        Map<String, Object> types = new LinkedHashMap<>();

        types.put("authorization_code", Map.of(
                "name", "授权码模式",
                "description", "最安全的模式，适用于有后端的 Web 应用",
                "flow", "用户授权 → 获取授权码 → 后端用授权码换 Token"));

        types.put("client_credentials", Map.of(
                "name", "客户端凭证模式",
                "description", "服务间调用，无用户参与",
                "flow", "直接用 client_id + client_secret 获取 Token"));

        types.put("refresh_token", Map.of(
                "name", "刷新令牌模式",
                "description", "Access Token 过期后，用 Refresh Token 获取新令牌",
                "flow", "用 refresh_token 获取新的 access_token"));

        types.put("deprecated", Map.of(
                "password", "密码模式 - 不安全，仅用于高度信任的第一方应用",
                "implicit", "隐式模式 - Token 暴露在 URL 中，请改用 PKCE"));

        return types;
    }

    /**
     * 获取 OAuth2 角色说明
     */
    public Map<String, String> getRolesInfo() {
        Map<String, String> roles = new LinkedHashMap<>();
        roles.put("Resource Owner", "用户（资源拥有者）");
        roles.put("Client", "第三方应用（需要访问资源）");
        roles.put("Authorization Server", "授权服务器（颁发令牌）");
        roles.put("Resource Server", "资源服务器（存储受保护资源）");
        return roles;
    }

    // ==================== 内部类 ====================

    public static class ClientInfo {
        public String clientId;
        public String secret;
        public String redirectUri;
        public List<String> scopes;

        public ClientInfo(String clientId, String secret, String redirectUri, List<String> scopes) {
            this.clientId = clientId;
            this.secret = secret;
            this.redirectUri = redirectUri;
            this.scopes = scopes;
        }
    }

    public static class AuthorizationCode {
        public String code;
        public String clientId;
        public Long userId;
        public String redirectUri;
        public String scope;
        public String state;
        public long createdAt;
        public long expiresAt;
    }
}
