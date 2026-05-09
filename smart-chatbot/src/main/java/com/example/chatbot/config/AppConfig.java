package com.example.chatbot.config;

import com.example.chatbot.memory.ChatMemoryManager;
import com.example.chatbot.rag.FaqRetriever;
import com.example.chatbot.service.CustomerServiceBot;
import com.example.chatbot.tools.OrderTools;
import com.example.chatbot.tools.ProductTools;
import com.example.chatbot.tools.TicketTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.service.AiServices;

import java.time.Duration;

/**
 * 应用配置类
 * 
 * 支持多种大模型提供商：
 * - Gemini (默认)
 * - OpenAI
 * - Azure OpenAI
 * - Ollama (本地)
 * - Demo (演示模式)
 */
public class AppConfig {

    /**
     * 模型类型枚举
     */
    public enum ModelType {
        GEMINI, // Google Gemini (默认)
        OPENAI, // OpenAI API
        AZURE, // Azure OpenAI
        OLLAMA, // 本地 Ollama
        DEMO // 演示模式（Mock）
    }

    private final ModelType modelType;
    private final ChatLanguageModel chatModel;
    private final ChatMemoryManager memoryManager;
    private final OrderTools orderTools;
    private final ProductTools productTools;
    private final TicketTools ticketTools;
    private final FaqRetriever faqRetriever;

    public AppConfig(ModelType modelType) {
        this.modelType = modelType;
        this.chatModel = createChatModel();
        this.memoryManager = new ChatMemoryManager(20);
        this.orderTools = new OrderTools();
        this.productTools = new ProductTools();
        this.ticketTools = new TicketTools();
        this.faqRetriever = new FaqRetriever();
    }

    /**
     * 根据配置创建 Chat 模型
     */
    private ChatLanguageModel createChatModel() {
        return switch (modelType) {
            case GEMINI -> createGeminiModel();
            case OPENAI -> createOpenAiModel();
            case AZURE -> createAzureOpenAiModel();
            case OLLAMA -> createOllamaModel();
            case DEMO -> createDemoModel();
        };
    }

    /**
     * 创建 Google Gemini 模型 (默认)
     */
    private ChatLanguageModel createGeminiModel() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getenv("GOOGLE_AI_API_KEY");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("⚠️ 未设置 GEMINI_API_KEY，切换到演示模式");
            return createDemoModel();
        }

        String modelName = System.getenv().getOrDefault("GEMINI_MODEL", "gemini-1.5-flash");

        System.out.println("🚀 使用 Gemini 模型: " + modelName);

        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 创建 OpenAI 模型
     */
    private ChatLanguageModel createOpenAiModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("⚠️ 未设置 OPENAI_API_KEY，切换到演示模式");
            return createDemoModel();
        }

        String modelName = System.getenv().getOrDefault("OPENAI_MODEL", "gpt-4o");
        String baseUrl = System.getenv("OPENAI_BASE_URL"); // 支持自定义 endpoint

        System.out.println("🚀 使用 OpenAI 模型: " + modelName);

        var builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3);

        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
        }

        return builder.build();
    }

    /**
     * 创建 Azure OpenAI 模型
     */
    private ChatLanguageModel createAzureOpenAiModel() {
        String apiKey = System.getenv("AZURE_OPENAI_API_KEY");
        String endpoint = System.getenv("AZURE_OPENAI_ENDPOINT");
        String deploymentName = System.getenv().getOrDefault("AZURE_OPENAI_DEPLOYMENT", "gpt-4");

        if (apiKey == null || apiKey.isEmpty() || endpoint == null || endpoint.isEmpty()) {
            System.err.println("⚠️ 未设置 AZURE_OPENAI_API_KEY 或 AZURE_OPENAI_ENDPOINT，切换到演示模式");
            return createDemoModel();
        }

        System.out.println("🚀 使用 Azure OpenAI 部署: " + deploymentName);

        return AzureOpenAiChatModel.builder()
                .apiKey(apiKey)
                .endpoint(endpoint)
                .deploymentName(deploymentName)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }

    /**
     * 创建 Ollama 本地模型
     */
    private ChatLanguageModel createOllamaModel() {
        String baseUrl = System.getenv().getOrDefault("OLLAMA_URL", "http://localhost:11434");
        String modelName = System.getenv().getOrDefault("OLLAMA_MODEL", "llama3");

        System.out.println("🚀 使用 Ollama 模型: " + modelName + " @ " + baseUrl);

        try {
            return OllamaChatModel.builder()
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .temperature(0.7)
                    .timeout(Duration.ofSeconds(120))
                    .build();
        } catch (Exception e) {
            System.err.println("⚠️ 无法连接 Ollama，切换到演示模式: " + e.getMessage());
            return createDemoModel();
        }
    }

    /**
     * 创建演示模式模型（简单的规则匹配）
     */
    private ChatLanguageModel createDemoModel() {
        System.out.println("📌 运行在演示模式（无实际 LLM 调用）");
        return new DemoChatModel(orderTools, productTools, ticketTools, faqRetriever);
    }

    /**
     * 创建智能客服机器人
     */
    public CustomerServiceBot createChatbot() {
        if (modelType == ModelType.DEMO) {
            return createDemoChatbot();
        }

        return AiServices.builder(CustomerServiceBot.class)
                .chatLanguageModel(chatModel)
                .chatMemoryProvider(memoryManager::getOrCreateMemory)
                .tools(orderTools, productTools, ticketTools, faqRetriever)
                .build();
    }

    /**
     * 创建演示模式机器人
     */
    private CustomerServiceBot createDemoChatbot() {
        return (userId, message) -> {
            DemoChatModel demo = (DemoChatModel) chatModel;
            return demo.processMessage(message);
        };
    }

    /**
     * 解析模型类型字符串
     */
    public static ModelType parseModelType(String type) {
        if (type == null || type.isEmpty()) {
            return ModelType.GEMINI; // 默认使用 Gemini
        }
        return switch (type.toLowerCase()) {
            case "gemini", "google" -> ModelType.GEMINI;
            case "openai", "gpt" -> ModelType.OPENAI;
            case "azure", "azure-openai" -> ModelType.AZURE;
            case "ollama", "local" -> ModelType.OLLAMA;
            case "demo", "mock" -> ModelType.DEMO;
            default -> {
                System.out.println("⚠️ 未知模型类型: " + type + "，使用默认 Gemini");
                yield ModelType.GEMINI;
            }
        };
    }

    /**
     * 打印支持的模型信息
     */
    public static void printSupportedModels() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                  支持的模型提供商                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.println("║  gemini   │ Google Gemini (默认)     │ GEMINI_API_KEY   ║");
        System.out.println("║  openai   │ OpenAI GPT               │ OPENAI_API_KEY   ║");
        System.out.println("║  azure    │ Azure OpenAI             │ AZURE_OPENAI_*   ║");
        System.out.println("║  ollama   │ Ollama 本地模型          │ 无需 API Key     ║");
        System.out.println("║  demo     │ 演示模式                 │ 无需 API Key     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }

    // Getters
    public ChatLanguageModel getChatModel() {
        return chatModel;
    }

    public ChatMemoryManager getMemoryManager() {
        return memoryManager;
    }

    public OrderTools getOrderTools() {
        return orderTools;
    }

    public ProductTools getProductTools() {
        return productTools;
    }

    public TicketTools getTicketTools() {
        return ticketTools;
    }

    public FaqRetriever getFaqRetriever() {
        return faqRetriever;
    }

    public ModelType getModelType() {
        return modelType;
    }
}
