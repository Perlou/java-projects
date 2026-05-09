package com.example.seckill.observability.logging;

import com.example.seckill.observability.tracing.TraceContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Phase 18: 请求日志过滤器
 * 
 * 自动记录 HTTP 请求和响应信息
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        try {
            // 继续处理请求
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();

            // 获取 TraceId
            String traceId = null;
            TraceContext ctx = TraceContext.current();
            if (ctx != null) {
                traceId = ctx.getTraceId();
            }

            // 构建日志消息
            String logMessage = String.format(
                    "{\"type\":\"http\",\"method\":\"%s\",\"uri\":\"%s\",\"query\":\"%s\"," +
                            "\"status\":%d,\"durationMs\":%d,\"traceId\":\"%s\"}",
                    method, uri, queryString != null ? queryString : "",
                    status, duration, traceId != null ? traceId : "");

            // 根据状态码选择日志级别
            if (status >= 500) {
                log.error(logMessage);
            } else if (status >= 400) {
                log.warn(logMessage);
            } else {
                log.info(logMessage);
            }
        }
    }
}
