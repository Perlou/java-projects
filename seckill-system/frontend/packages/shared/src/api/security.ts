import request from "./request";

/**
 * Phase 19: 安全架构 API
 */
export const securityApi = {
  // ========== JWT Token ==========

  // 生成 JWT Token
  generateToken(userId: number, username: string, role: string = "USER") {
    return request.post<any>(
      `/demo/security/jwt/generate?userId=${userId}&username=${username}&role=${role}`
    );
  },

  // 验证 Token
  validateToken(token: string) {
    return request.post<any>(`/demo/security/jwt/validate?token=${token}`);
  },

  // Token 信息说明
  getTokenInfo() {
    return request.get<any>("/demo/security/jwt/info");
  },

  // ========== OAuth2 ==========

  // OAuth2 授权模式说明
  getGrantTypes() {
    return request.get<any>("/demo/security/oauth2/grant-types");
  },

  // OAuth2 角色说明
  getOAuth2Roles() {
    return request.get<any>("/demo/security/oauth2/roles");
  },

  // 生成 PKCE 参数
  generatePKCE() {
    return request.post<any>("/demo/security/oauth2/pkce/generate");
  },

  // ========== RBAC 权限 ==========

  // 获取所有角色
  getAllRoles() {
    return request.get<any>("/demo/security/rbac/roles");
  },

  // RBAC 模型说明
  getRBACModels() {
    return request.get<any>("/demo/security/rbac/models");
  },

  // 分配角色
  assignRole(userId: number, role: string) {
    return request.post<any>(
      `/demo/security/rbac/assign?userId=${userId}&role=${role}`
    );
  },

  // 检查权限
  checkPermission(userId: number, resource: string, action: string) {
    return request.get<any>(
      `/demo/security/rbac/check?userId=${userId}&resource=${resource}&action=${action}`
    );
  },

  // 权限命名规范
  getPermissionNaming() {
    return request.get<any>("/demo/security/rbac/naming");
  },

  // ========== 零信任 ==========

  // 零信任核心原则
  getZeroTrustPrinciples() {
    return request.get<any>("/demo/security/zerotrust/principles");
  },

  // 零信任架构组件
  getZeroTrustComponents() {
    return request.get<any>("/demo/security/zerotrust/components");
  },

  // 实施阶段
  getImplementationPhases() {
    return request.get<any>("/demo/security/zerotrust/phases");
  },

  // 风险评估
  assessRisk(params: {
    userId?: number;
    deviceId?: string;
    ipAddress?: string;
    hasValidToken?: boolean;
    sensitiveOperation?: boolean;
  }) {
    const searchParams = new URLSearchParams();
    if (params.userId) searchParams.append("userId", params.userId.toString());
    if (params.deviceId) searchParams.append("deviceId", params.deviceId);
    if (params.ipAddress) searchParams.append("ipAddress", params.ipAddress);
    if (params.hasValidToken !== undefined)
      searchParams.append("hasValidToken", params.hasValidToken.toString());
    if (params.sensitiveOperation !== undefined)
      searchParams.append(
        "sensitiveOperation",
        params.sensitiveOperation.toString()
      );
    return request.post<any>(`/demo/security/zerotrust/assess?${searchParams}`);
  },

  // ========== 安全编码 ==========

  // OWASP Top 10
  getOWASPTop10() {
    return request.get<any>("/demo/security/coding/owasp");
  },

  // XSS 编码演示
  demonstrateXSSEncoding(input: string) {
    return request.post<any>(
      `/demo/security/coding/xss-encode?input=${encodeURIComponent(input)}`
    );
  },

  // 安全响应头
  getSecurityHeaders() {
    return request.get<any>("/demo/security/coding/headers");
  },

  // 安全编码原则
  getSecureCodingPrinciples() {
    return request.get<any>("/demo/security/coding/principles");
  },

  // 输入验证
  validateInput(username?: string, email?: string) {
    const params = new URLSearchParams();
    if (username) params.append("username", username);
    if (email) params.append("email", email);
    return request.post<any>(`/demo/security/coding/validate?${params}`);
  },

  // ========== 概览 ==========

  getOverview() {
    return request.get<any>("/demo/security/overview");
  },
};
