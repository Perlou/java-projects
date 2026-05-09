import request from "./request";

/**
 * Phase 18: 可观测性 API
 */
export const observabilityApi = {
  // ========== 链路追踪 ==========

  // 获取当前请求 TraceId
  getCurrentTrace() {
    return request.get<any>("/demo/observability/trace/current");
  },

  // 创建自定义 Span
  createSpan(operationName: string, durationMs: number = 100) {
    return request.post<any>(
      `/demo/observability/trace/span?operationName=${operationName}&durationMs=${durationMs}`
    );
  },

  // 链路追踪概念
  getTracingConcepts() {
    return request.get<any>("/demo/observability/trace/concepts");
  },

  // 最近的 Span 记录
  getRecentSpans(limit: number = 10) {
    return request.get<any>(`/demo/observability/trace/recent?limit=${limit}`);
  },

  // ========== 指标监控 ==========

  // 秒杀业务指标
  getSeckillMetrics() {
    return request.get<any>("/demo/observability/metrics/seckill");
  },

  // 指标类型说明
  getMetricTypes() {
    return request.get<any>("/demo/observability/metrics/types");
  },

  // PromQL 查询示例
  getPromQLExamples() {
    return request.get<any>("/demo/observability/metrics/promql");
  },

  // RED 方法
  getREDMethod() {
    return request.get<any>("/demo/observability/metrics/red");
  },

  // ========== 结构化日志 ==========

  // 记录结构化日志
  logStructured(event: string, userId?: number, message?: string) {
    const params = new URLSearchParams({ event });
    if (userId) params.append("userId", userId.toString());
    if (message) params.append("message", message);
    return request.post<any>(`/demo/observability/log/structured?${params}`);
  },

  // 日志级别规范
  getLogLevels() {
    return request.get<any>("/demo/observability/log/levels");
  },

  // 日志最佳实践
  getLogBestPractices() {
    return request.get<any>("/demo/observability/log/best-practices");
  },

  // ========== 告警规则 ==========

  // 告警规则列表
  getAlertRules() {
    return request.get<any>("/demo/observability/alerts");
  },

  // 执行告警检测
  checkAlerts() {
    return request.post<any>("/demo/observability/alerts/check");
  },

  // 告警历史
  getAlertHistory(limit: number = 20) {
    return request.get<any>(
      `/demo/observability/alerts/history?limit=${limit}`
    );
  },

  // 告警级别说明
  getAlertLevels() {
    return request.get<any>("/demo/observability/alerts/levels");
  },

  // 告警类型说明
  getAlertTypes() {
    return request.get<any>("/demo/observability/alerts/types");
  },

  // ========== 概览 ==========

  getOverview() {
    return request.get<any>("/demo/observability/overview");
  },
};
