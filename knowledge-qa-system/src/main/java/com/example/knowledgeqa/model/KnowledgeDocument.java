package com.example.knowledgeqa.model;

/**
 * 知识文档
 */
public record KnowledgeDocument(
        String id,
        String title,
        String content,
        String source,
        String category) {
}
