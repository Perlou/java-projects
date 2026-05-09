# 企业知识库问答系统 (Enterprise Knowledge QA)

> 基于 RAG + LangChain4j + Spring Boot 的知识库问答系统

## 🎯 项目简介

这是一个完整的 RAG (检索增强生成) 系统，演示企业知识库问答的核心功能：

- ✅ **文档解析**: 自动加载和解析 Markdown 文档
- ✅ **语义切分**: 基于语义的文档切分策略
- ✅ **向量检索**: 高效的相似度搜索
- ✅ **多模型支持**: Gemini / OpenAI / Ollama
- ✅ **多轮对话**: 支持上下文记忆

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                          │
│                  (Spring Boot MVC)                          │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                            │
│              QaService (RAG 核心流程)                       │
├──────────────────────┬──────────────────────────────────────┤
│    KnowledgeLoader   │        KnowledgeRetriever           │
│   (文档加载/切分)     │         (向量检索)                   │
├──────────────────────┴──────────────────────────────────────┤
│                    LangChain4j                              │
│     EmbeddingModel  │  EmbeddingStore  │  ChatModel        │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 快速开始

### 使用 Gemini (默认)

```bash
export GEMINI_API_KEY=your-gemini-api-key
cd projects/knowledge-qa-system
mvn spring-boot:run
```

### 使用 OpenAI

```bash
export LLM_PROVIDER=openai
export OPENAI_API_KEY=sk-xxx
mvn spring-boot:run
```

### 使用 Ollama 本地模型

```bash
export LLM_PROVIDER=ollama
mvn spring-boot:run
```

### 访问地址

- API: http://localhost:8083
- Swagger UI: http://localhost:8083/swagger-ui.html

## 📡 API 接口

### 问答接口

```bash
# POST 方式
curl -X POST http://localhost:8083/api/qa/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "你们的产品如何定价？"}'

# GET 方式
curl "http://localhost:8083/api/qa/ask?question=如何获取API Key"
```

### 检索接口

```bash
# 仅检索相关文档（不调用 LLM）
curl "http://localhost:8083/api/qa/search?query=退货政策&topK=3"
```

### 知识库管理

```bash
# 查看统计
curl http://localhost:8083/api/qa/stats

# 动态添加知识
curl -X POST http://localhost:8083/api/qa/knowledge \
  -H "Content-Type: application/json" \
  -d '{"text": "新产品上线了！", "source": "announcement"}'
```

## 📁 项目结构

```
src/main/java/com/example/knowledgeqa/
├── KnowledgeQaApplication.java    # 启动类
├── config/
│   └── LlmConfig.java             # 多模型配置
├── model/
│   ├── QaRequest.java             # 请求模型
│   ├── QaResponse.java            # 响应模型
│   └── SourceDocument.java        # 来源文档
├── rag/
│   ├── KnowledgeLoader.java       # 知识加载器
│   └── KnowledgeRetriever.java    # 知识检索器
├── service/
│   └── QaService.java             # 问答服务
└── controller/
    └── QaController.java          # REST API

src/main/resources/
├── application.yml                # 配置文件
└── knowledge/                     # 知识库文档
    ├── faq.md                     # 常见问题
    └── user_manual.md             # 使用手册
```

## ⚙️ 配置说明

| 环境变量         | 说明           | 默认值                   |
| ---------------- | -------------- | ------------------------ |
| `LLM_PROVIDER`   | 模型提供商     | `gemini`                 |
| `GEMINI_API_KEY` | Gemini API Key | -                        |
| `OPENAI_API_KEY` | OpenAI API Key | -                        |
| `OLLAMA_URL`     | Ollama 地址    | `http://localhost:11434` |

## 📚 RAG 核心流程

```
用户提问
    ↓
① 问题向量化 (EmbeddingModel)
    ↓
② 向量检索 (EmbeddingStore)
    ↓
③ 获取相关文档 (Top-K)
    ↓
④ 构建 Prompt (系统提示 + 上下文 + 问题)
    ↓
⑤ LLM 生成回答 (ChatModel)
    ↓
返回结果 (答案 + 来源)
```

---

