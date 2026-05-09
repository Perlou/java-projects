package com.example.knowledgeqa.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问答响应
 */
public record QaResponse(
        String answer,
        List<SourceDocument> sources,
        String sessionId,
        long responseTimeMs,
        LocalDateTime timestamp) {
    public QaResponse(String answer, List<SourceDocument> sources, String sessionId, long responseTimeMs) {
        this(answer, sources, sessionId, responseTimeMs, LocalDateTime.now());
    }
}
