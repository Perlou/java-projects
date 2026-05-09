package com.example.knowledgeqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业知识库问答系统 - Spring Boot 主入口
 * 
 * Phase 26 实战项目：基于 RAG 的知识库问答系统
 * 
 * 核心功能：
 * - 文档解析与向量化
 * - 语义检索
 * - 大模型问答
 * - 多模型支持 (Gemini/OpenAI/Ollama)
 * 
 * @author Java Course
 * @since Phase 26
 */
@SpringBootApplication
public class KnowledgeQaApplication {

    public static void main(String[] args) {
        printBanner();
        SpringApplication.run(KnowledgeQaApplication.class, args);
    }

    private static void printBanner() {
        System.out.println("""

                ╔══════════════════════════════════════════════════════════╗
                ║       📚 企业知识库问答系统 - Phase 26 实战项目          ║
                ║       基于 RAG + LangChain4j + Spring Boot              ║
                ╚══════════════════════════════════════════════════════════╝

                【系统启动中...】
                """);
    }
}
