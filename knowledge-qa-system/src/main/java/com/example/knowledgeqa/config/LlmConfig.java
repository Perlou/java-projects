package com.example.knowledgeqa.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * LLM 配置类
 * 
 * 支持多种模型提供商：
 * - Gemini (默认)
 * - OpenAI
 * - Ollama
 */
@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Value("${llm.provider:gemini}")
    private String provider;

    @Value("${llm.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${llm.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${llm.openai.api-key:}")
    private String openaiApiKey;

    @Value("${llm.openai.model:gpt-4o}")
    private String openaiModel;

    @Value("${llm.openai.base-url:}")
    private String openaiBaseUrl;

    @Value("${llm.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${llm.ollama.model:llama3}")
    private String ollamaModel;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("配置 LLM 提供商: {}", provider);

        return switch (provider.toLowerCase()) {
            case "gemini", "google" -> createGeminiModel();
            case "openai", "gpt" -> createOpenAiModel();
            case "ollama", "local" -> createOllamaModel();
            default -> {
                log.warn("未知提供商: {}，使用 Gemini", provider);
                yield createGeminiModel();
            }
        };
    }

    private ChatLanguageModel createGeminiModel() {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("未配置 GEMINI_API_KEY，使用 Mock 模式");
            return new MockChatModel();
        }
        log.info("使用 Gemini 模型: {}", geminiModel);
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(geminiModel)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    private ChatLanguageModel createOpenAiModel() {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            log.warn("未配置 OPENAI_API_KEY，使用 Mock 模式");
            return new MockChatModel();
        }
        log.info("使用 OpenAI 模型: {}", openaiModel);
        var builder = OpenAiChatModel.builder()
                .apiKey(openaiApiKey)
                .modelName(openaiModel)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60));

        if (openaiBaseUrl != null && !openaiBaseUrl.isEmpty()) {
            builder.baseUrl(openaiBaseUrl);
        }
        return builder.build();
    }

    private ChatLanguageModel createOllamaModel() {
        log.info("使用 Ollama 模型: {} @ {}", ollamaModel, ollamaBaseUrl);
        try {
            return OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl)
                    .modelName(ollamaModel)
                    .temperature(0.7)
                    .timeout(Duration.ofSeconds(120))
                    .build();
        } catch (Exception e) {
            log.warn("无法连接 Ollama，使用 Mock 模式: {}", e.getMessage());
            return new MockChatModel();
        }
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("使用本地嵌入模型: AllMiniLmL6V2");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("使用内存向量存储");
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Mock 模型（无 API Key 时使用）
     */
    private static class MockChatModel implements ChatLanguageModel {

        @Override
        public String generate(String prompt) {
            return "【演示模式】这是一个模拟回答。请配置有效的 API Key 以获取真实响应。";
        }

        @Override
        public Response<AiMessage> generate(List<ChatMessage> messages) {
            String response = "【演示模式】这是一个模拟回答。请配置有效的 API Key 以获取真实响应。";
            return Response.from(AiMessage.from(response));
        }
    }
}
