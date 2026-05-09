# Phase 24 实战项目 - 企业级数据仓库系统 🏢

> 基于 Spring Boot 的数据仓库管理系统，模拟数仓核心功能

## 📖 项目介绍

本项目演示企业级数据仓库的核心管理功能：

### 核心功能

| 功能         | 说明                           |
| ------------ | ------------------------------ |
| 数仓分层管理 | ODS/DWD/DWS/ADS/DIM 五层架构   |
| 数据质量检测 | 完整性、唯一性、有效性、一致性 |
| 数据血缘追踪 | SQL 解析、上下游关系、影响分析 |
| ETL 任务管理 | 任务调度、依赖管理、执行监控   |

---

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.8+

### 运行项目

```bash
cd projects/data-warehouse-system

# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

### 访问地址

- 应用: http://localhost:8082
- Swagger UI: http://localhost:8082/swagger-ui.html
- H2 控制台: http://localhost:8082/h2-console

---

## 📡 API 接口

### 表管理

```bash
# 获取所有表
curl http://localhost:8082/api/tables

# 按分层获取
curl http://localhost:8082/api/tables/layer/DWD

# 数仓架构概览
curl http://localhost:8082/api/tables/architecture
```

### 数据质量

```bash
# 获取质量规则
curl http://localhost:8082/api/quality/rules

# 执行全部检测
curl -X POST http://localhost:8082/api/quality/check/all

# 检测指定表
curl -X POST http://localhost:8082/api/quality/check/table/1
```

### 数据血缘

```bash
# 获取表血缘
curl http://localhost:8082/api/lineage/8

# 获取上游血缘
curl http://localhost:8082/api/lineage/8/upstream

# 解析 SQL 血缘
curl -X POST http://localhost:8082/api/lineage/parse \
  -H "Content-Type: application/json" \
  -d '{"sql":"INSERT INTO dwd_order SELECT * FROM ods_order JOIN dim_user"}'

# 血缘图数据
curl http://localhost:8082/api/lineage/graph
```

### ETL 任务

```bash
# 获取所有任务
curl http://localhost:8082/api/etl/tasks

# 执行任务
curl -X POST http://localhost:8082/api/etl/execute/1

# 任务依赖图
curl http://localhost:8082/api/etl/dependencies
```

---

## 🏗️ 项目结构

```
src/main/java/com/example/warehouse/
├── DataWarehouseApplication.java
├── model/                       # 数据模型
│   ├── DwLayer.java             # 分层枚举
│   ├── TableMetadata.java       # 表元数据
│   ├── ColumnMetadata.java      # 列元数据
│   ├── QualityRule.java         # 质量规则
│   ├── QualityResult.java       # 检测结果
│   ├── LineageEdge.java         # 血缘边
│   └── EtlTask.java             # ETL 任务
├── engine/                      # 核心引擎
│   ├── SqlLineageParser.java    # SQL 血缘解析
│   ├── QualityChecker.java      # 质量检测
│   └── MockEtlExecutor.java     # ETL 执行器
├── repository/                  # 数据访问
├── service/                     # 服务层
└── controller/                  # REST API
```

---

## 📚 数仓知识点

### 数仓分层 (DwLayer)

```
ODS  → 原始数据层 (Operational Data Store)
DWD  → 明细数据层 (Data Warehouse Detail)
DWS  → 服务数据层 (Data Warehouse Service)
ADS  → 应用数据层 (Application Data Service)
DIM  → 维度表层   (Dimension)
```

### 数据质量六维度

- **完整性**: 数据是否缺失
- **唯一性**: 是否有重复
- **有效性**: 格式/范围是否正确
- **一致性**: 多源是否一致
- **时效性**: 数据是否及时
- **准确性**: 数据是否正确

### 数据血缘

- **上游血缘**: 数据从哪来
- **下游血缘**: 数据去哪里
- **影响分析**: 变更影响范围

---

> 💡 更多数仓知识请参考 Phase 24 课件：`src/main/java/phase24/`
