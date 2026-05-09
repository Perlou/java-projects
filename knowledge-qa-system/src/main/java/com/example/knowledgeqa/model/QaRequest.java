package com.example.knowledgeqa.model;

/**
 * 问答请求
 */
public record QaRequest(
        String question,
        String sessionId,
        Integer topK) {
    public QaRequest {
        if (topK == null)
            topK = 5;
    }

    public QaRequest(String question) {
        this(question, null, 5);
    }
}
