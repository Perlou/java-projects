package com.example.knowledgeqa.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库加载器
 * 
 * 负责加载、切分和向量化知识文档
 */
@Component
public class KnowledgeLoader {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeLoader.class);

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Value("${rag.chunk-size:500}")
    private int chunkSize;

    @Value("${rag.chunk-overlap:50}")
    private int chunkOverlap;

    @Value("${rag.knowledge-path:classpath:knowledge/}")
    private String knowledgePath;

    private int documentCount = 0;
    private int chunkCount = 0;

    public KnowledgeLoader(EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @PostConstruct
    public void init() {
        log.info("【知识库加载器】初始化");
        log.info("  切分参数: chunkSize={}, overlap={}", chunkSize, chunkOverlap);
        loadKnowledgeBase();
    }

    /**
     * 加载知识库
     */
    public void loadKnowledgeBase() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(knowledgePath + "*.md");

            log.info("发现 {} 个知识文档", resources.length);

            for (Resource resource : resources) {
                loadDocument(resource);
            }

            log.info("【知识库加载完成】文档: {}, 切片: {}", documentCount, chunkCount);

        } catch (Exception e) {
            log.error("加载知识库失败: {}", e.getMessage());
        }
    }

    /**
     * 加载单个文档
     */
    private void loadDocument(Resource resource) {
        try {
            String filename = resource.getFilename();
            log.info("加载文档: {}", filename);

            // 读取内容
            String content;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                content = reader.lines().collect(Collectors.joining("\n"));
            }

            // 创建 Document
            Document document = Document.from(content, Metadata.from("source", filename));

            // 切分
            DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
            List<TextSegment> segments = splitter.split(document);

            log.info("  -> 切分为 {} 个片段", segments.size());

            // 向量化并存储
            for (TextSegment segment : segments) {
                var embedding = embeddingModel.embed(segment.text()).content();
                embeddingStore.add(embedding, segment);
                chunkCount++;
            }

            documentCount++;

        } catch (Exception e) {
            log.error("加载文档失败 {}: {}", resource.getFilename(), e.getMessage());
        }
    }

    /**
     * 添加文本到知识库
     */
    public int addText(String text, String source) {
        Document document = Document.from(text, Metadata.from("source", source));
        DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);
        List<TextSegment> segments = splitter.split(document);

        for (TextSegment segment : segments) {
            var embedding = embeddingModel.embed(segment.text()).content();
            embeddingStore.add(embedding, segment);
        }

        log.info("添加文本: source={}, chunks={}", source, segments.size());
        return segments.size();
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }
}
