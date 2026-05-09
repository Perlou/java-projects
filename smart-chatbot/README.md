# 智能客服机器人 (Smart Customer Service Chatbot)

> 基于 LangChain4j 的智能客服系统

## 🎯 项目简介

支持多种大模型提供商的智能客服机器人：

| 提供商                   | 模型参数 | 环境变量                                         |
| ------------------------ | -------- | ------------------------------------------------ |
| **Google Gemini** (默认) | `gemini` | `GEMINI_API_KEY`                                 |
| OpenAI                   | `openai` | `OPENAI_API_KEY`                                 |
| Azure OpenAI             | `azure`  | `AZURE_OPENAI_API_KEY` + `AZURE_OPENAI_ENDPOINT` |
| Ollama 本地              | `ollama` | 无需 Key                                         |
| 演示模式                 | `demo`   | 无需 Key                                         |

## 🚀 快速开始

### 使用 Gemini (推荐)

```bash
export GEMINI_API_KEY=your-gemini-api-key
mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication"
```

### 使用 OpenAI

```bash
export OPENAI_API_KEY=sk-your-key
mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication" -Dmodel=openai
```

### 使用 Azure OpenAI

```bash
export AZURE_OPENAI_API_KEY=your-azure-key
export AZURE_OPENAI_ENDPOINT=https://your-resource.openai.azure.com
export AZURE_OPENAI_DEPLOYMENT=gpt-4
mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication" -Dmodel=azure
```

### 使用 Ollama 本地模型

```bash
ollama pull llama3
mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication" -Dmodel=ollama
```

### 演示模式 (无需 API)

```bash
mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication" -Dmodel=demo
```

## ⚙️ 环境变量

| 变量                      | 说明            | 默认值                   |
| ------------------------- | --------------- | ------------------------ |
| `GEMINI_API_KEY`          | Gemini API Key  | -                        |
| `GEMINI_MODEL`            | 模型名称        | `gemini-1.5-flash`       |
| `OPENAI_API_KEY`          | OpenAI API Key  | -                        |
| `OPENAI_MODEL`            | 模型名称        | `gpt-4o`                 |
| `OPENAI_BASE_URL`         | 自定义 API 地址 | -                        |
| `AZURE_OPENAI_API_KEY`    | Azure Key       | -                        |
| `AZURE_OPENAI_ENDPOINT`   | Azure 端点      | -                        |
| `AZURE_OPENAI_DEPLOYMENT` | 部署名称        | `gpt-4`                  |
| `OLLAMA_URL`              | Ollama 地址     | `http://localhost:11434` |
| `OLLAMA_MODEL`            | 模型名称        | `llama3`                 |

## 🏗️ 项目结构

```
src/main/java/com/example/chatbot/
├── ChatbotApplication.java      # 主入口
├── config/
│   ├── AppConfig.java           # 多模型配置工厂
│   └── DemoChatModel.java       # 演示模型
├── service/
│   └── CustomerServiceBot.java  # AI Service
├── tools/
│   ├── OrderTools.java          # 订单查询
│   ├── ProductTools.java        # 商品查询
│   └── TicketTools.java         # 工单处理
├── memory/
│   └── ChatMemoryManager.java   # 会话管理
├── rag/
│   ├── KnowledgeLoader.java     # 知识加载
│   └── FaqRetriever.java        # FAQ检索
└── model/
    ├── Order.java
    ├── Product.java
    └── Ticket.java
```

## 💬 使用示例

```
╔══════════════════════════════════════════════════════════╗
║         🤖 智能客服机器人 - 多模型版本                    ║
╚══════════════════════════════════════════════════════════╝

🔧 当前模型: Google Gemini

> 我的订单 ORD-12345 到哪了？
🤖 您的订单 ORD-12345 目前正在配送中，预计明天下午送达。

> iPhone 15 还有货吗？
🤖 iPhone 15 目前有货，库存充足。价格：¥6999

> 你们的退货政策是什么？
🤖 商品签收后7天内可申请无理由退货，退款3-5个工作日内原路返还。
```

## 🔧 核心技术

- **LangChain4j**: AI 应用开发框架
- **多模型支持**: Gemini / OpenAI / Azure / Ollama
- **Function Calling**: 工具函数调用
- **对话记忆**: 多用户会话隔离
- **简化版 RAG**: 基于内存的知识检索

---

