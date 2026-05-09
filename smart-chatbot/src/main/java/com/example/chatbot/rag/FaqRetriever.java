package com.example.chatbot.rag;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.*;

/**
 * FAQ 检索器
 * 
 * 基于关键词匹配的简化版 RAG 实现
 * 用于回答常见问题
 */
public class FaqRetriever {

    private final KnowledgeLoader knowledgeLoader;

    public FaqRetriever() {
        this.knowledgeLoader = new KnowledgeLoader();
    }

    public FaqRetriever(KnowledgeLoader knowledgeLoader) {
        this.knowledgeLoader = knowledgeLoader;
    }

    /**
     * 搜索常见问题
     * 作为工具提供给 AI 使用
     */
    @Tool("搜索常见问题知识库，获取政策、流程等信息")
    public String searchFaq(@P("搜索关键词，如：退货、发票、配送") String query) {
        if (query == null || query.trim().isEmpty()) {
            return "请提供搜索关键词。";
        }

        String queryLower = query.toLowerCase();
        List<MatchResult> results = new ArrayList<>();

        for (KnowledgeLoader.FaqEntry faq : knowledgeLoader.getAllFaqs()) {
            int score = calculateMatchScore(queryLower, faq.getKeywords());
            if (score > 0) {
                results.add(new MatchResult(faq, score));
            }
        }

        if (results.isEmpty()) {
            return "未找到与 \"" + query + "\" 相关的常见问题。请尝试其他关键词，或联系人工客服。";
        }

        // 按匹配度排序
        results.sort((a, b) -> Integer.compare(b.score, a.score));

        // 返回最佳匹配
        return results.get(0).faq.getAnswer();
    }

    /**
     * 搜索多个相关问题
     */
    @Tool("搜索多个相关的常见问题")
    public String searchMultipleFaqs(
            @P("搜索关键词") String query,
            @P("返回结果数量，1-5") int limit) {

        if (query == null || query.trim().isEmpty()) {
            return "请提供搜索关键词。";
        }

        limit = Math.max(1, Math.min(5, limit));
        String queryLower = query.toLowerCase();
        List<MatchResult> results = new ArrayList<>();

        for (KnowledgeLoader.FaqEntry faq : knowledgeLoader.getAllFaqs()) {
            int score = calculateMatchScore(queryLower, faq.getKeywords());
            if (score > 0) {
                results.add(new MatchResult(faq, score));
            }
        }

        if (results.isEmpty()) {
            return "未找到相关问题。";
        }

        results.sort((a, b) -> Integer.compare(b.score, a.score));

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(Math.min(limit, results.size())).append(" 个相关问题:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        for (int i = 0; i < Math.min(limit, results.size()); i++) {
            sb.append(results.get(i).faq.getAnswer());
            if (i < Math.min(limit, results.size()) - 1) {
                sb.append("\n\n");
            }
        }

        return sb.toString();
    }

    /**
     * 计算匹配分数
     */
    private int calculateMatchScore(String query, List<String> keywords) {
        int score = 0;

        for (String keyword : keywords) {
            String keywordLower = keyword.toLowerCase();

            // 完全匹配
            if (query.equals(keywordLower)) {
                score += 100;
            }
            // 包含关键词
            else if (query.contains(keywordLower)) {
                score += 50;
            }
            // 关键词包含在查询中
            else if (keywordLower.contains(query)) {
                score += 30;
            }
            // 简单的字符重叠检测
            else {
                int overlap = countCharOverlap(query, keywordLower);
                if (overlap > query.length() / 2) {
                    score += 10;
                }
            }
        }

        return score;
    }

    /**
     * 计算字符重叠数
     */
    private int countCharOverlap(String s1, String s2) {
        Set<Character> set1 = new HashSet<>();
        for (char c : s1.toCharArray()) {
            set1.add(c);
        }

        int count = 0;
        for (char c : s2.toCharArray()) {
            if (set1.contains(c)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 列出所有可用的 FAQ 主题
     */
    @Tool("列出所有可查询的常见问题主题")
    public String listFaqTopics() {
        StringBuilder sb = new StringBuilder();
        sb.append("可查询的常见问题主题:\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━\n");

        Set<String> topics = new LinkedHashSet<>();
        for (KnowledgeLoader.FaqEntry faq : knowledgeLoader.getAllFaqs()) {
            if (!faq.getKeywords().isEmpty()) {
                topics.add(faq.getKeywords().get(0));
            }
        }

        int i = 1;
        for (String topic : topics) {
            sb.append(i++).append(". ").append(topic).append("\n");
        }

        return sb.toString();
    }

    /**
     * 匹配结果
     */
    private static class MatchResult {
        final KnowledgeLoader.FaqEntry faq;
        final int score;

        MatchResult(KnowledgeLoader.FaqEntry faq, int score) {
            this.faq = faq;
            this.score = score;
        }
    }
}
