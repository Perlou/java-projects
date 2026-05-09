# 实时风控系统 🛡️

> 基于 Spring Boot 的实时风控系统，模拟 Flink CEP、State、Window 等核心概念

## 📖 项目介绍

本项目演示如何使用 Flink 的核心概念构建实时风控系统：

### Flink 概念对照

| 项目组件             | Flink 概念          | 说明                       |
| -------------------- | ------------------- | -------------------------- |
| `StateManager`       | Keyed State         | 用户独立状态（画像、历史） |
| `PatternMatcher`     | CEP                 | 复杂事件模式匹配           |
| `RuleEngine`         | Broadcast State     | 动态规则广播               |
| `WindowAggregator`   | Window              | 时间窗口聚合               |
| `TransactionService` | DataStream Pipeline | 数据流处理管线             |

### 风控场景

1. **连续登录失败检测** - CEP Pattern: `begin().next().next().within()`
2. **异地交易检测** - CEP Pattern: `begin().followedBy().where()`
3. **大额交易检测** - CEP Pattern: `begin().times(n).where()`
4. **规则引擎** - 动态风控规则匹配

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.8+

### 运行项目

```bash
cd projects/risk-control-system

# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

### 访问地址

- 应用: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- API 文档: http://localhost:8081/api-docs

---

## 📡 API 接口

### 交易 API

```bash
# 提交交易
curl -X POST http://localhost:8081/api/transaction \
  -H "Content-Type: application/json" \
  -d '{"userId":"user001","amount":50000,"city":"北京","transactionType":"PAYMENT"}'

# 获取用户风险画像
curl http://localhost:8081/api/transaction/profile/user001
```

### CEP 演示 API

```bash
# 演示连续登录失败检测
curl -X POST "http://localhost:8081/api/demo/cep/login-failure?userId=user001&failCount=3"

# 演示异地交易检测
curl -X POST "http://localhost:8081/api/demo/cep/cross-city?userId=user002"

# 演示大额交易检测
curl -X POST "http://localhost:8081/api/demo/cep/high-amount?userId=user003&amount=60000&count=3"

# 查看 Flink 概念对照
curl http://localhost:8081/api/demo/flink-concepts
```

### 规则 API

```bash
# 获取所有规则
curl http://localhost:8081/api/rules

# 添加规则
curl -X POST http://localhost:8081/api/rules \
  -H "Content-Type: application/json" \
  -d '{"ruleName":"测试规则","ruleType":"THRESHOLD","targetField":"amount","operator":"GT","threshold":"10000","action":"ALERT","riskScore":10}'
```

### 告警 API

```bash
# 获取所有告警
curl http://localhost:8081/api/alerts

# 告警统计
curl http://localhost:8081/api/alerts/stats
```

---

## 🏗️ 项目结构

```
src/main/java/com/example/riskcontrol/
├── RiskControlApplication.java    # 启动类
├── model/                         # 数据模型
│   ├── Transaction.java           # 交易事件
│   ├── LoginEvent.java            # 登录事件
│   ├── RiskRule.java              # 风控规则
│   ├── Alert.java                 # 告警
│   └── UserRiskProfile.java       # 用户风险画像
├── engine/                        # 核心引擎
│   ├── PatternMatcher.java        # CEP 模式匹配
│   ├── RuleEngine.java            # 规则引擎
│   └── RiskScoreCalculator.java   # 风险评分
├── processor/                     # 处理器
│   ├── StateManager.java          # 状态管理
│   └── WindowAggregator.java      # 窗口聚合
├── service/                       # 服务层
│   ├── TransactionService.java    # 交易服务
│   └── AlertService.java          # 告警服务
└── controller/                    # 控制器
    ├── TransactionController.java
    ├── RuleController.java
    ├── AlertController.java
    └── DemoController.java        # Flink 概念演示
```

---

## 📚 Flink 知识点

### 1. Keyed State（StateManager）

```java
// Flink 实现
ValueState<UserRiskProfile> profileState;
ListState<Transaction> transactionHistory;

// 本项目模拟
Map<String, UserRiskProfile> userProfileState = new ConcurrentHashMap<>();
Map<String, List<Transaction>> userTransactionHistory = new ConcurrentHashMap<>();
```

### 2. CEP 模式匹配（PatternMatcher）

```java
// Flink 实现
Pattern<LoginEvent> pattern = Pattern.<LoginEvent>begin("first")
    .where(e -> e.isFailed())
    .next("second").where(e -> e.isFailed())
    .next("third").where(e -> e.isFailed())
    .within(Time.minutes(5));

// 本项目模拟
List<LoginEvent> failedEvents = events.stream()
    .filter(LoginEvent::isFailed)
    .filter(e -> e.getEventTime().isAfter(windowStart))
    .toList();
```

### 3. Broadcast State（RuleEngine）

```java
// Flink 实现
BroadcastStream<RiskRule> ruleBroadcast = ruleStream.broadcast(ruleDescriptor);
stream.connect(ruleBroadcast).process(new BroadcastProcessFunction<>() {...});

// 本项目模拟
Map<String, RiskRule> ruleStore = new ConcurrentHashMap<>();
```

### 4. Window（WindowAggregator）

```java
// Flink 实现
stream.keyBy(tx -> tx.getUserId())
      .window(TumblingProcessingTimeWindows.of(Time.seconds(60)))
      .aggregate(new TransactionAggregator());

// 本项目模拟
LocalDateTime windowStart = calculateTumblingWindowStart(eventTime);
WindowStats stats = windows.computeIfAbsent(windowKey, WindowStats::new);
stats.addTransaction(tx);
```

---

