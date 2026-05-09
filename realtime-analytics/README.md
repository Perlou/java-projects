# 实时数据分析平台

> 基于 Spring Boot + Apache Spark 的实时数据分析平台

## 🎯 项目概述

本项目是 **Spark 生态与实时计算** 的实战项目，展示如何将 Spark 集成到 Spring Boot 企业级应用中。

## ✨ 核心功能

- 📊 **实时数据分析** - Spark Streaming 实时处理用户行为数据
- 🔍 **批量数据分析** - Spark SQL 进行离线聚合分析
- 📈 **指标计算** - PV/UV、用户路径、转化率等业务指标
- 🚀 **REST API** - 提供数据查询和分析触发接口

## 🏗️ 技术栈

| 技术            | 版本  | 说明           |
| --------------- | ----- | -------------- |
| Spring Boot     | 3.2.0 | 应用框架       |
| Apache Spark    | 3.5.0 | 大数据处理引擎 |
| Spark SQL       | 3.5.0 | SQL 查询引擎   |
| Spark Streaming | 3.5.0 | 流处理引擎     |
| Redis           | -     | 实时指标缓存   |
| H2/MySQL        | -     | 数据持久化     |

## 📦 项目结构

```
src/main/java/com/example/analytics/
├── RealtimeAnalyticsApplication.java   # 启动类
├── config/
│   ├── SparkConfig.java                # Spark 配置
│   └── RedisConfig.java                # Redis 配置
├── model/
│   ├── UserAction.java                 # 用户行为模型
│   ├── MetricResult.java               # 指标结果
│   └── AnalyticsReport.java            # 分析报告
├── service/
│   ├── DataIngestionService.java       # 数据采集
│   ├── SparkBatchService.java          # 批处理服务
│   ├── SparkStreamService.java         # 流处理服务
│   └── MetricsService.java             # 指标服务
├── processor/
│   ├── UserBehaviorProcessor.java      # 用户行为处理
│   ├── RealTimeAggregator.java         # 实时聚合
│   └── WindowAnalyzer.java             # 窗口分析
└── controller/
    ├── AnalyticsController.java        # 分析 API
    └── DataIngestionController.java    # 数据接入 API
```

## 🚀 快速开始

### ⚠️ Java 版本要求

> **重要**: Spark 3.5 需要 **Java 11 或 Java 17**
>
> Java 18+ 存在兼容性问题（`Subject.getSubject()` 已被移除）

如果您使用的是 Java 18+，建议：

- 使用 [SDKMAN!](https://sdkman.io/) 管理多版本 Java
- 或设置 `JAVA_HOME` 指向 Java 17

```bash
# 使用 SDKMAN! 安装 Java 17
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

### 1. 编译项目

```bash
mvn clean compile
```

### 2. 运行应用

```bash
# 方式1：使用启动脚本（推荐）
./run.sh

# 方式2：直接使用 Maven
mvn spring-boot:run
```

### 3. 访问 API 文档

打开浏览器访问: http://localhost:8080/swagger-ui.html

## 📚 核心知识点

本项目涵盖的核心知识点：

1. **RDD 操作** - 转换算子与行动算子
2. **DataFrame/Dataset** - 结构化数据处理
3. **Spark SQL** - SQL 查询与 UDF
4. **DAG 执行模型** - Stage 划分与 Task 调度
5. **Catalyst 优化器** - 查询优化原理
6. **Spark Streaming** - 微批处理模型
7. **Structured Streaming** - 增量查询
8. **性能调优** - 内存管理与数据倾斜处理

## 🔧 配置说明

### application.yml 主要配置

```yaml
spark:
  app-name: realtime-analytics
  master: local[*] # 本地模式，使用所有可用核心

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 📊 API 接口

| 接口                          | 方法 | 说明             |
| ----------------------------- | ---- | ---------------- |
| `/api/data/ingest`            | POST | 接收用户行为数据 |
| `/api/data/batch-ingest`      | POST | 批量数据导入     |
| `/api/analytics/realtime`     | GET  | 获取实时指标     |
| `/api/analytics/batch/{type}` | POST | 执行批量分析     |
| `/api/analytics/report`       | GET  | 获取分析报告     |

## 📝 学习要点

> 💡 **提示**: 本项目使用 Spark Local 模式，无需搭建集群环境即可学习 Spark 核心概念。

重点关注：

- `SparkConfig.java` - 了解 SparkSession 配置
- `SparkBatchService.java` - 学习 DataFrame 操作和 SQL 查询
- `RealTimeAggregator.java` - 理解 RDD 操作和 DAG 执行
- `WindowAnalyzer.java` - 掌握窗口分析概念

---

