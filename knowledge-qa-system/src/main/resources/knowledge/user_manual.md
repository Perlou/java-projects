# 产品使用手册

## 第一章：快速入门

### 1.1 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- 可用内存 4GB 以上

### 1.2 安装步骤

1. 下载安装包

```bash
curl -O https://download.example.com/sdk-latest.zip
unzip sdk-latest.zip
```

2. 配置环境变量

```bash
export EXAMPLE_HOME=/path/to/sdk
export PATH=$PATH:$EXAMPLE_HOME/bin
```

3. 验证安装

```bash
example-cli version
```

### 1.3 第一个应用

创建 HelloWorld.java：

```java
import com.example.sdk.Client;

public class HelloWorld {
    public static void main(String[] args) {
        Client client = Client.builder()
            .apiKey(System.getenv("API_KEY"))
            .build();

        String response = client.chat("Hello!");
        System.out.println(response);
    }
}
```

## 第二章：核心概念

### 2.1 客户端配置

客户端支持多种配置方式：

| 配置项  | 说明         | 默认值   |
| ------- | ------------ | -------- |
| apiKey  | API 密钥     | 必填     |
| timeout | 超时时间(秒) | 30       |
| retries | 重试次数     | 3        |
| baseUrl | API 地址     | 默认云端 |

### 2.2 对话管理

支持多轮对话记忆：

```java
ChatSession session = client.createSession();
session.chat("我叫张三");
session.chat("我叫什么名字？"); // 会记住上文
```

### 2.3 工具调用

注册自定义工具：

```java
@Tool("获取天气信息")
public String getWeather(@P("城市名") String city) {
    return weatherService.query(city);
}
```

## 第三章：高级功能

### 3.1 RAG 知识库

构建私有知识库：

1. 准备文档（PDF、Word、Markdown）
2. 调用文档加载 API
3. 自动切分和向量化
4. 查询时自动检索相关内容

### 3.2 Agent 模式

创建智能代理：

```java
Agent agent = Agent.builder()
    .model(chatModel)
    .tools(searchTool, calculatorTool)
    .memory(chatMemory)
    .build();

agent.run("帮我查询北京天气并转换为华氏度");
```

### 3.3 流式输出

支持流式响应：

```java
client.streamChat("讲一个故事", token -> {
    System.out.print(token); // 逐 token 输出
});
```

## 第四章：最佳实践

### 4.1 Prompt 设计

好的 Prompt 技巧：

- 明确角色定义
- 提供示例（Few-shot）
- 指定输出格式
- 设置约束条件

### 4.2 成本优化

- 选择合适的模型（小任务用小模型）
- 设置合理的 max_tokens
- 缓存常见请求
- 批量处理

### 4.3 错误处理

```java
try {
    String response = client.chat(message);
} catch (RateLimitException e) {
    // 限流，等待重试
    Thread.sleep(e.getRetryAfter());
} catch (TokenLimitException e) {
    // 输入过长，需要截断
}
```

## 附录

### A. 错误码列表

| 错误码 | 说明       | 解决方案           |
| ------ | ---------- | ------------------ |
| 401    | 认证失败   | 检查 API Key       |
| 429    | 请求过多   | 降低频率或升级套餐 |
| 500    | 服务器错误 | 稍后重试           |

### B. 更新日志

- v2.0.0: 新增 Agent 功能
- v1.5.0: 支持流式输出
- v1.0.0: 初始版本
