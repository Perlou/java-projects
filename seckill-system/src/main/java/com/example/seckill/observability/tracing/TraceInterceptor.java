package com.example.seckill.observability.tracing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Phase 18: 链路追踪拦截器
 * 
 * 自动为每个 HTTP 请求创建或传播 TraceContext
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String SPAN_HEADER = "X-Span-Id";
    private static final String W3C_TRACE_HEADER = "traceparent";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) {
        TraceContext context;

        // 尝试从 W3C Trace Context 头获取
        String traceParent = request.getHeader(W3C_TRACE_HEADER);
        if (traceParent != null && !traceParent.isEmpty()) {
            context = TraceContext.fromW3CTraceContext(traceParent);
        }
        // 尝试从自定义头获取
        else {
            String traceId = request.getHeader(TRACE_HEADER);
            String parentSpanId = request.getHeader(SPAN_HEADER);
            if (traceId != null && !traceId.isEmpty()) {
                context = new TraceContext(traceId, parentSpanId);
            } else {
                context = new TraceContext();
            }
        }

        // 设置到 ThreadLocal
        TraceContext.setCurrent(context);

        // 设置到 MDC，供日志使用
        MDC.put("traceId", context.getTraceId());
        MDC.put("spanId", context.getSpanId());

        // 设置响应头
        response.setHeader(TRACE_HEADER, context.getTraceId());
        response.setHeader(SPAN_HEADER, context.getSpanId());

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 清理
        TraceContext.clear();
        MDC.clear();
    }
}
