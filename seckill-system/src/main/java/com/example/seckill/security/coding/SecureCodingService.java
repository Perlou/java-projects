package com.example.seckill.security.coding;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Phase 19: 安全编码服务
 * 
 * 提供安全编码工具和 OWASP Top 10 防御演示
 */
@Service
public class SecureCodingService {

    // 输入验证正则
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$");

    // ==================== 输入验证 ====================

    /**
     * 验证用户名
     */
    public ValidationResult validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new ValidationResult(false, "用户名不能为空");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, "用户名只能包含字母、数字和下划线，长度3-20");
        }
        return new ValidationResult(true, "验证通过");
    }

    /**
     * 验证邮箱
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return new ValidationResult(false, "邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "邮箱格式不正确");
        }
        return new ValidationResult(true, "验证通过");
    }

    // ==================== XSS 防御 ====================

    /**
     * HTML 上下文编码（防 XSS）
     */
    public String encodeForHtml(String input) {
        if (input == null)
            return null;
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * JavaScript 上下文编码（防 XSS）
     */
    public String encodeForJavaScript(String input) {
        if (input == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append(String.format("\\u%04x", (int) c));
            }
        }
        return sb.toString();
    }

    /**
     * 演示 XSS 编码效果
     */
    public Map<String, Object> demonstrateXSSEncoding(String maliciousInput) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalInput", maliciousInput);
        result.put("htmlEncoded", encodeForHtml(maliciousInput));
        result.put("jsEncoded", encodeForJavaScript(maliciousInput));
        result.put("explanation", "编码后的内容会被浏览器作为文本显示，而非执行为脚本");
        return result;
    }

    // ==================== OWASP Top 10 ====================

    /**
     * 获取 OWASP Top 10 (2021)
     */
    public List<Map<String, Object>> getOWASPTop10() {
        List<Map<String, Object>> top10 = new ArrayList<>();

        top10.add(owaspItem("A01", "Broken Access Control", "访问控制失效",
                "确保用户只能访问其授权的资源",
                List.of("实施 RBAC/ABAC", "默认拒绝", "记录访问日志")));

        top10.add(owaspItem("A02", "Cryptographic Failures", "加密失效",
                "敏感数据未加密或使用弱加密",
                List.of("使用强加密算法", "HTTPS 传输", "密钥安全管理")));

        top10.add(owaspItem("A03", "Injection", "注入攻击",
                "SQL/NoSQL/OS/LDAP 注入",
                List.of("使用预编译语句", "输入验证", "ORM 框架")));

        top10.add(owaspItem("A04", "Insecure Design", "不安全设计",
                "设计阶段的安全缺陷",
                List.of("威胁建模", "安全设计模式", "安全需求")));

        top10.add(owaspItem("A05", "Security Misconfiguration", "安全配置错误",
                "默认配置、不必要的功能、详细错误信息",
                List.of("安全加固", "最小化安装", "自动化配置检查")));

        top10.add(owaspItem("A06", "Vulnerable Components", "易受攻击的组件",
                "使用含有已知漏洞的第三方组件",
                List.of("定期更新依赖", "漏洞扫描", "软件成分分析")));

        top10.add(owaspItem("A07", "Identification and Auth Failures", "身份认证失败",
                "弱密码、会话管理问题",
                List.of("MFA 多因素认证", "密码策略", "Session 安全")));

        top10.add(owaspItem("A08", "Software and Data Integrity", "软件和数据完整性",
                "CI/CD 管道被篡改、不安全的反序列化",
                List.of("签名验证", "安全 CI/CD", "完整性检查")));

        top10.add(owaspItem("A09", "Security Logging and Monitoring", "安全日志和监控不足",
                "无法检测、响应安全事件",
                List.of("完整的审计日志", "实时监控告警", "事件响应计划")));

        top10.add(owaspItem("A10", "Server-Side Request Forgery", "服务端请求伪造 (SSRF)",
                "服务器被利用发起恶意请求",
                List.of("URL 白名单", "禁用不必要的协议", "网络隔离")));

        return top10;
    }

    private Map<String, Object> owaspItem(String id, String name, String nameCn,
            String description, List<String> mitigations) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("nameCn", nameCn);
        item.put("description", description);
        item.put("mitigations", mitigations);
        return item;
    }

    // ==================== 安全响应头 ====================

    /**
     * 获取推荐的安全响应头
     */
    public Map<String, Object> getSecurityHeaders() {
        Map<String, Object> headers = new LinkedHashMap<>();

        headers.put("Content-Security-Policy", Map.of(
                "value", "default-src 'self'",
                "purpose", "限制脚本/资源来源，防止 XSS"));

        headers.put("X-Content-Type-Options", Map.of(
                "value", "nosniff",
                "purpose", "防止 MIME 类型嗅探"));

        headers.put("X-Frame-Options", Map.of(
                "value", "DENY",
                "purpose", "防止点击劫持 (Clickjacking)"));

        headers.put("Strict-Transport-Security", Map.of(
                "value", "max-age=31536000; includeSubDomains",
                "purpose", "强制 HTTPS (HSTS)"));

        headers.put("X-XSS-Protection", Map.of(
                "value", "1; mode=block",
                "purpose", "浏览器 XSS 过滤器（已过时，CSP 替代）"));

        return headers;
    }

    /**
     * 获取安全编码核心原则
     */
    public List<String> getSecureCodingPrinciples() {
        return List.of(
                "不信任用户输入 - 验证、清理、编码",
                "使用安全 API - 预编译语句、加密库",
                "最小权限原则 - 只授予必要权限",
                "深度防御 - 多层安全措施",
                "保持更新 - 及时修复已知漏洞");
    }

    // ==================== 内部类 ====================

    public static class ValidationResult {
        public boolean valid;
        public String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }
}
