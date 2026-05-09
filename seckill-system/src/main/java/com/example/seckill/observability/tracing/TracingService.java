package com.example.seckill.observability.tracing;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 18: 链路追踪服务
 * 
 * 管理 Span 生命周期，记录链路信息
 */
@Service
public class TracingService {

    // 存储活跃的 Span（演示用）
    private final Map<String, SpanInfo> activeSpans = new ConcurrentHashMap<>();

    // 已完成的 Span（演示用，实际应发送到收集器）
    private final List<SpanInfo> completedSpans = Collections.synchronizedList(new ArrayList<>());

    /**
     * 开始一个新 Span
     */
    public SpanInfo startSpan(String operationName) {
        TraceContext ctx = TraceContext.current();
        if (ctx == null) {
            ctx = new TraceContext();
            TraceContext.setCurrent(ctx);
        }

        SpanInfo span = new SpanInfo();
        span.traceId = ctx.getTraceId();
        span.spanId = ctx.getSpanId();
        span.parentSpanId = ctx.getParentSpanId();
        span.operationName = operationName;
        span.startTime = System.currentTimeMillis();
        span.tags = new HashMap<>();

        activeSpans.put(span.spanId, span);
        return span;
    }

    /**
     * 结束 Span
     */
    public void endSpan(SpanInfo span) {
        span.endTime = System.currentTimeMillis();
        span.duration = span.endTime - span.startTime;

        activeSpans.remove(span.spanId);
        completedSpans.add(span);

        // 保持最近 100 条
        while (completedSpans.size() > 100) {
            completedSpans.remove(0);
        }
    }

    /**
     * 为 Span 添加标签
     */
    public void addTag(SpanInfo span, String key, String value) {
        span.tags.put(key, value);
    }

    /**
     * 记录错误
     */
    public void recordError(SpanInfo span, Exception e) {
        span.error = true;
        span.errorMessage = e.getMessage();
        span.tags.put("error.type", e.getClass().getName());
    }

    /**
     * 获取当前链路信息
     */
    public Map<String, Object> getCurrentTraceInfo() {
        TraceContext ctx = TraceContext.current();
        Map<String, Object> info = new LinkedHashMap<>();

        if (ctx != null) {
            info.put("traceId", ctx.getTraceId());
            info.put("spanId", ctx.getSpanId());
            info.put("parentSpanId", ctx.getParentSpanId());
            info.put("w3cTraceContext", ctx.toW3CTraceContext());
            info.put("durationMs", ctx.getDurationMs());
        } else {
            info.put("status", "No active trace context");
        }

        return info;
    }

    /**
     * 获取最近的 Span 记录（演示用）
     */
    public List<SpanInfo> getRecentSpans(int limit) {
        int size = completedSpans.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(completedSpans.subList(start, size));
    }

    /**
     * 获取链路追踪概念说明
     */
    public Map<String, Object> getTracingConcepts() {
        Map<String, Object> concepts = new LinkedHashMap<>();

        concepts.put("Trace", "一次完整请求的链路，由多个 Span 组成");
        concepts.put("Span", "一个操作单元（如 HTTP 请求、DB 查询）");
        concepts.put("TraceId", "全局唯一标识，贯穿整个链路");
        concepts.put("SpanId", "当前操作的唯一标识");
        concepts.put("ParentSpanId", "父操作，用于构建调用树");

        concepts.put("contextPropagation", Map.of(
                "HTTP Header", "X-Trace-Id, X-Span-Id 或 traceparent (W3C)",
                "MessageQueue", "消息属性中携带追踪上下文",
                "Thread", "使用 ThreadLocal 传递"));

        concepts.put("tools", Map.of(
                "Jaeger", "云原生，CNCF 项目",
                "SkyWalking", "Java 应用推荐，无侵入",
                "Zipkin", "简单易用，Spring 生态"));

        return concepts;
    }

    // ==================== Span 信息类 ====================

    public static class SpanInfo {
        public String traceId;
        public String spanId;
        public String parentSpanId;
        public String operationName;
        public long startTime;
        public long endTime;
        public long duration;
        public Map<String, String> tags;
        public boolean error;
        public String errorMessage;
    }
}
