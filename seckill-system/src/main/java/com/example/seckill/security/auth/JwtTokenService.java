package com.example.seckill.security.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

/**
 * Phase 19: JWT Token 管理服务
 * 
 * 提供 JWT 生成、验证、解析功能
 */
@Component
public class JwtTokenService {

    // 使用安全的密钥
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token 有效期
    private static final long ACCESS_TOKEN_EXPIRE_MS = 15 * 60 * 1000; // 15 分钟
    private static final long REFRESH_TOKEN_EXPIRE_MS = 7 * 24 * 60 * 60 * 1000; // 7 天

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_MS);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_MS);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString()) // Token ID，用于撤销
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 验证 Token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 Token Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从 Token 获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Token 生命周期信息
     */
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("accessTokenExpireMinutes", ACCESS_TOKEN_EXPIRE_MS / 1000 / 60);
        info.put("refreshTokenExpireDays", REFRESH_TOKEN_EXPIRE_MS / 1000 / 60 / 60 / 24);

        info.put("tokenTypes", Map.of(
                "AccessToken", "短期有效（15分钟），用于 API 调用",
                "RefreshToken", "长期有效（7天），用于获取新 Access Token",
                "IDToken", "OIDC 标准，包含用户身份信息"));

        info.put("storageStrategy", Map.of(
                "后端应用", "内存/Redis（加密存储）",
                "浏览器SPA", "HttpOnly Cookie（避免 localStorage）",
                "移动App", "Secure Keychain / Keystore"));

        return info;
    }
}
