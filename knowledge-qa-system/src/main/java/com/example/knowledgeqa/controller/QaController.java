package com.example.knowledgeqa.controller;

import com.example.knowledgeqa.model.QaRequest;
import com.example.knowledgeqa.model.QaResponse;
import com.example.knowledgeqa.model.SourceDocument;
import com.example.knowledgeqa.rag.KnowledgeLoader;
import com.example.knowledgeqa.rag.KnowledgeRetriever;
import com.example.knowledgeqa.service.QaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 问答控制器
 */
@RestController
@RequestMapping("/api/qa")
@Tag(name = "知识问答", description = "基于 RAG 的知识库问答 API")
public class QaController {

    private final QaService qaService;
    private final KnowledgeRetriever retriever;
    private final KnowledgeLoader loader;

    public QaController(QaService qaService,
            KnowledgeRetriever retriever,
            KnowledgeLoader loader) {
        this.qaService = qaService;
        this.retriever = retriever;
        this.loader = loader;
    }

    @PostMapping("/ask")
    @Operation(summary = "问答", description = "基于知识库的问答")
    public ResponseEntity<QaResponse> ask(@RequestBody QaRequest request) {
        return ResponseEntity.ok(qaService.answer(request));
    }

    @GetMapping("/ask")
    @Operation(summary = "问答 (GET)", description = "简单问答接口")
    public ResponseEntity<QaResponse> askGet(
            @RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "5") Integer topK) {
        QaRequest request = new QaRequest(question, sessionId, topK);
        return ResponseEntity.ok(qaService.answer(request));
    }

    @GetMapping("/search")
    @Operation(summary = "检索", description = "检索相关文档（不调用 LLM）")
    public ResponseEntity<List<SourceDocument>> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "5") Integer topK) {
        return ResponseEntity.ok(retriever.retrieve(query, topK));
    }

    @PostMapping("/session/{sessionId}/clear")
    @Operation(summary = "清除会话")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        qaService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("success", true, "message", "会话已清除"));
    }

    @GetMapping("/stats")
    @Operation(summary = "知识库统计")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "documents", loader.getDocumentCount(),
                "chunks", loader.getChunkCount()));
    }

    @PostMapping("/knowledge")
    @Operation(summary = "添加知识", description = "动态添加文本到知识库")
    public ResponseEntity<Map<String, Object>> addKnowledge(
            @RequestBody Map<String, String> request) {
        String text = request.get("text");
        String source = request.getOrDefault("source", "user_input");
        int chunks = loader.addText(text, source);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "chunks", chunks,
                "message", "已添加 " + chunks + " 个知识片段"));
    }
}
