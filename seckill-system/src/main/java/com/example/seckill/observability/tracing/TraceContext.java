package com.example.seckill.observability.tracing;

import java.util.UUID;

/**
 * Phase 18: 链路追踪上下文
 * 
 * 管理 TraceId 和 SpanId，用于分布式链路追踪
 */
public class TraceContext {

    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private final String traceId;
    private String spanId;
    private String parentSpanId;
    private final long startTime;

    public TraceContext() {
        this.traceId = generateId();
        this.spanId = generateId();
        this.parentSpanId = null;
        this.startTime = System.currentTimeMillis();
    }

    public TraceContext(String traceId, String parentSpanId) {
        this.traceId = traceId;
        this.spanId = generateId();
        this.parentSpanId = parentSpanId;
        this.startTime = System.currentTimeMillis();
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // ==================== 静态方法 ====================

    public static TraceContext current() {
        return CURRENT.get();
    }

    public static void setCurrent(TraceContext context) {
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static String currentTraceId() {
        TraceContext ctx = current();
        return ctx != null ? ctx.getTraceId() : null;
    }

    public static String currentSpanId() {
        TraceContext ctx = current();
        return ctx != null ? ctx.getSpanId() : null;
    }

    // ==================== Span 操作 ====================

    /**
     * 创建子 Span
     */
    public TraceContext createChildSpan() {
        return new TraceContext(this.traceId, this.spanId);
    }

    /**
     * 计算当前 Span 耗时
     */
    public long getDurationMs() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 生成 W3C Trace Context 格式
     * 格式: 00-{traceId}-{spanId}-01
     */
    public String toW3CTraceContext() {
        return String.format("00-%s-%s-01", traceId, spanId);
    }

    /**
     * 从 W3C Trace Context 解析
     */
    public static TraceContext fromW3CTraceContext(String traceParent) {
        if (traceParent == null || traceParent.isEmpty()) {
            return new TraceContext();
        }
        String[] parts = traceParent.split("-");
        if (parts.length >= 3) {
            return new TraceContext(parts[1], parts[2]);
        }
        return new TraceContext();
    }

    // ==================== Getters ====================

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return String.format("TraceContext{traceId='%s', spanId='%s', parentSpanId='%s'}",
                traceId, spanId, parentSpanId);
    }
}
