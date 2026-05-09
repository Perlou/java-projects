package com.example.knowledgeqa.rag;

import com.example.knowledgeqa.model.SourceDocument;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识检索器
 * 
 * 基于向量相似度检索相关文档
 */
@Component
public class KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetriever.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${rag.top-k:5}")
    private int defaultTopK;

    @Value("${rag.min-score:0.5}")
    private double minScore;

    public KnowledgeRetriever(EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    /**
     * 检索相关文档
     */
    public List<SourceDocument> retrieve(String query, Integer topK) {
        int k = topK != null ? topK : defaultTopK;

        log.debug("检索: query='{}', topK={}", query, k);

        // 向量化查询
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 相似度搜索
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(k)
                .minScore(minScore)
                .build();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();

        log.debug("找到 {} 个相关片段", matches.size());

        // 转换结果
        return matches.stream()
                .map(match -> new SourceDocument(
                        match.embedded().text(),
                        match.embedded().metadata().getString("source"),
                        match.score()))
                .collect(Collectors.toList());
    }

    /**
     * 检索并格式化为上下文
     */
    public String retrieveAsContext(String query, Integer topK) {
        List<SourceDocument> docs = retrieve(query, topK);

        if (docs.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("【相关知识】\n\n");

        for (int i = 0; i < docs.size(); i++) {
            SourceDocument doc = docs.get(i);
            context.append(String.format("[%d] (来源: %s, 相关度: %.2f)\n",
                    i + 1, doc.source(), doc.score()));
            context.append(doc.content());
            context.append("\n\n");
        }

        return context.toString();
    }
}
