package com.example.knowledgeqa.service;

import com.example.knowledgeqa.model.QaRequest;
import com.example.knowledgeqa.model.QaResponse;
import com.example.knowledgeqa.model.SourceDocument;
import com.example.knowledgeqa.rag.KnowledgeRetriever;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问答服务
 * 
 * 核心 RAG 流程：
 * 1. 接收用户问题
 * 2. 检索相关知识
 * 3. 构建 Prompt
 * 4. 调用 LLM 生成回答
 */
@Service
public class QaService {

    private static final Logger log = LoggerFactory.getLogger(QaService.class);

    private final ChatLanguageModel chatModel;
    private final KnowledgeRetriever retriever;

    // 简单的会话历史（生产环境应使用 Redis 等）
    private final Map<String, StringBuilder> sessionHistory = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            你是一个专业的企业知识库助手。请根据提供的参考资料回答用户问题。

            回答要求：
            1. 只基于提供的参考资料回答，不要编造信息
            2. 如果资料中没有相关内容，请明确告知用户
            3. 回答要简洁、专业、有条理
            4. 可以适当使用列表或分点说明
            5. 在回答末尾可以询问是否需要更多信息
            """;

    public QaService(ChatLanguageModel chatModel, KnowledgeRetriever retriever) {
        this.chatModel = chatModel;
        this.retriever = retriever;
    }

    /**
     * 处理问答请求
     */
    public QaResponse answer(QaRequest request) {
        long startTime = System.currentTimeMillis();

        String sessionId = request.sessionId() != null
                ? request.sessionId()
                : UUID.randomUUID().toString().substring(0, 8);

        log.info("处理问答: session={}, question={}", sessionId, request.question());

        // 1. 检索相关文档
        List<SourceDocument> sources = retriever.retrieve(request.question(), request.topK());
        String context = formatContext(sources);

        log.info("检索到 {} 个相关片段", sources.size());

        // 2. 构建 Prompt
        String prompt = buildPrompt(request.question(), context, sessionId);

        // 3. 调用 LLM
        String answer;
        try {
            answer = chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("LLM 调用失败: {}", e.getMessage());
            answer = "抱歉，系统暂时无法处理您的请求。请稍后再试。";
        }

        // 4. 保存历史
        saveHistory(sessionId, request.question(), answer);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("问答完成: session={}, elapsed={}ms", sessionId, elapsed);

        return new QaResponse(answer, sources, sessionId, elapsed);
    }

    /**
     * 构建 Prompt
     */
    private String buildPrompt(String question, String context, String sessionId) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(SYSTEM_PROMPT).append("\n\n");

        // 添加上下文
        if (!context.isEmpty()) {
            prompt.append("【参考资料】\n");
            prompt.append(context);
            prompt.append("\n");
        }

        // 添加历史对话（如果有）
        String history = sessionHistory.getOrDefault(sessionId, new StringBuilder()).toString();
        if (!history.isEmpty()) {
            prompt.append("【对话历史】\n");
            prompt.append(history);
            prompt.append("\n");
        }

        // 当前问题
        prompt.append("【用户问题】\n");
        prompt.append(question);
        prompt.append("\n\n请回答：");

        return prompt.toString();
    }

    /**
     * 格式化检索结果
     */
    private String formatContext(List<SourceDocument> sources) {
        if (sources.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            SourceDocument doc = sources.get(i);
            sb.append(String.format("[%d] 来源: %s (相关度: %.2f)\n",
                    i + 1, doc.source(), doc.score()));
            sb.append(doc.content()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 保存对话历史
     */
    private void saveHistory(String sessionId, String question, String answer) {
        sessionHistory.computeIfAbsent(sessionId, k -> new StringBuilder())
                .append("用户: ").append(question).append("\n")
                .append("助手: ").append(answer).append("\n\n");

        // 限制历史长度
        StringBuilder history = sessionHistory.get(sessionId);
        if (history.length() > 5000) {
            sessionHistory.put(sessionId, new StringBuilder(
                    history.substring(history.length() - 3000)));
        }
    }

    /**
     * 清除会话历史
     */
    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
        log.info("清除会话: {}", sessionId);
    }
}
