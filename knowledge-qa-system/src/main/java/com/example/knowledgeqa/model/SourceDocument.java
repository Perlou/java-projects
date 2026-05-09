package com.example.knowledgeqa.model;

/**
 * 来源文档
 */
public record SourceDocument(
        String content,
        String source,
        double score) {
}
