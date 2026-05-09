package com.example.chatbot;

import com.example.chatbot.config.AppConfig;
import com.example.chatbot.config.AppConfig.ModelType;
import com.example.chatbot.service.CustomerServiceBot;

import java.util.Scanner;
import java.util.UUID;

/**
 * 智能客服机器人 - 主入口
 * 
 * Phase 25 实战项目：综合运用大模型技术构建客服系统
 * 
 * 支持的模型提供商：
 * - Gemini (默认): 设置 GEMINI_API_KEY
 * - OpenAI: 设置 OPENAI_API_KEY
 * - Azure OpenAI: 设置 AZURE_OPENAI_API_KEY + AZURE_OPENAI_ENDPOINT
 * - Ollama: 启动本地 Ollama 服务
 * - Demo: 无需任何配置
 * 
 * 运行方式：
 * mvn compile exec:java
 * -Dexec.mainClass="com.example.chatbot.ChatbotApplication" -Dmodel=gemini
 * 
 * @author Java Course
 * @version 2.0
 */
public class ChatbotApplication {

    private static final String BANNER = """

            ╔══════════════════════════════════════════════════════════╗
            ║         🤖 智能客服机器人 - 多模型版本                    ║
            ║         支持: Gemini | OpenAI | Azure | Ollama           ║
            ╚══════════════════════════════════════════════════════════╝
            """;

    private static final String WELCOME = """

            您好！我是智能客服小助手，可以帮您：
            • 📦 查询订单状态和物流
            • 🛍️ 了解商品信息和库存
            • 💰 处理退款和换货申请
            • ❓ 回答常见问题

            请输入您的问题 (输入 'quit' 退出, 'help' 查看帮助):

            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """;

    public static void main(String[] args) {
        // 检查帮助参数
        for (String arg : args) {
            if (arg.equals("--help") || arg.equals("-h")) {
                AppConfig.printSupportedModels();
                printUsage();
                return;
            }
        }

        // 打印横幅
        System.out.println(BANNER);

        // 确定模型类型
        ModelType modelType = determineModelType(args);
        System.out.println("🔧 当前模型: " + getModelDescription(modelType));
        System.out.println();

        // 创建配置和机器人
        AppConfig config = new AppConfig(modelType);
        CustomerServiceBot chatbot = config.createChatbot();

        // 生成用户会话ID
        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        System.out.println("📝 会话ID: " + userId);

        // 打印欢迎信息
        System.out.println(WELCOME);

        // 开始交互
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // 读取用户输入
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();

                // 检查退出命令
                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("\n感谢使用，再见！👋\n");
                    break;
                }

                // 检查清除命令
                if (input.equalsIgnoreCase("clear")) {
                    config.getMemoryManager().clearMemory(userId);
                    System.out.println("✅ 对话记录已清除");
                    continue;
                }

                // 检查帮助命令
                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                // 检查模型信息命令
                if (input.equalsIgnoreCase("model")) {
                    AppConfig.printSupportedModels();
                    System.out.println("当前使用: " + getModelDescription(config.getModelType()));
                    continue;
                }

                // 空输入跳过
                if (input.isEmpty()) {
                    continue;
                }

                try {
                    // 调用 AI 获取回复
                    System.out.println("\n🤔 思考中...");
                    long startTime = System.currentTimeMillis();

                    String response = chatbot.chat(userId, input);

                    long elapsed = System.currentTimeMillis() - startTime;

                    // 打印回复
                    System.out.println("\n🤖 " + response);
                    System.out.printf("\n   [耗时: %dms]%n", elapsed);

                } catch (Exception e) {
                    System.err.println("\n❌ 处理出错: " + e.getMessage());
                    if (System.getenv("DEBUG") != null) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 确定模型类型
     */
    private static ModelType determineModelType(String[] args) {
        // 从命令行参数获取
        for (String arg : args) {
            if (arg.startsWith("-Dmodel=")) {
                String model = arg.substring("-Dmodel=".length());
                return AppConfig.parseModelType(model);
            }
        }

        // 从系统属性获取
        String modelProp = System.getProperty("model", "");
        if (!modelProp.isEmpty()) {
            return AppConfig.parseModelType(modelProp);
        }

        // 自动检测 (优先级: Gemini > OpenAI > Azure > Ollama > Demo)
        if (System.getenv("GEMINI_API_KEY") != null || System.getenv("GOOGLE_AI_API_KEY") != null) {
            return ModelType.GEMINI;
        }
        if (System.getenv("OPENAI_API_KEY") != null) {
            return ModelType.OPENAI;
        }
        if (System.getenv("AZURE_OPENAI_API_KEY") != null && System.getenv("AZURE_OPENAI_ENDPOINT") != null) {
            return ModelType.AZURE;
        }

        // 默认使用 Gemini（如果没有 key 会自动降级到 Demo）
        return ModelType.GEMINI;
    }

    /**
     * 获取模型描述
     */
    private static String getModelDescription(ModelType modelType) {
        return switch (modelType) {
            case GEMINI -> "Google Gemini";
            case OPENAI -> "OpenAI GPT";
            case AZURE -> "Azure OpenAI";
            case OLLAMA -> "Ollama 本地模型";
            case DEMO -> "演示模式 (规则匹配)";
        };
    }

    /**
     * 打印使用方法
     */
    private static void printUsage() {
        System.out.println("""

                使用方法:
                  mvn compile exec:java -Dexec.mainClass="com.example.chatbot.ChatbotApplication" [-Dmodel=<provider>]

                模型参数:
                  -Dmodel=gemini    使用 Google Gemini (默认)
                  -Dmodel=openai    使用 OpenAI GPT
                  -Dmodel=azure     使用 Azure OpenAI
                  -Dmodel=ollama    使用本地 Ollama
                  -Dmodel=demo      使用演示模式

                环境变量:
                  GEMINI_API_KEY          Google Gemini API Key
                  GEMINI_MODEL            模型名称 (默认: gemini-1.5-flash)
                  OPENAI_API_KEY          OpenAI API Key
                  OPENAI_MODEL            模型名称 (默认: gpt-4o)
                  OPENAI_BASE_URL         自定义 API 地址
                  AZURE_OPENAI_API_KEY    Azure OpenAI Key
                  AZURE_OPENAI_ENDPOINT   Azure 端点 URL
                  AZURE_OPENAI_DEPLOYMENT 部署名称
                  OLLAMA_URL              Ollama 地址 (默认: http://localhost:11434)
                  OLLAMA_MODEL            模型名称 (默认: llama3)
                """);
    }

    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("""

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📖 使用帮助
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

                【命令】
                quit  - 退出程序
                clear - 清除对话记录
                help  - 显示帮助信息
                model - 显示支持的模型

                【功能示例】
                • "查询订单 ORD-12345"
                • "iPhone 15 有货吗？"
                • "我要退款"
                • "你们的退货政策是什么？"

                【测试订单号】
                • ORD-12345 (配送中)
                • ORD-12346 (已签收)
                • ORD-12347 (已付款)

                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """);
    }
}
