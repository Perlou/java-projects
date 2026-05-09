# 日志分析系统

基于 Spring Boot 的 Web 日志分析系统，模拟 MapReduce 处理流程。

## 🎯 项目目标

使用 MapReduce 思想处理 Web 服务器日志，实现：

- **PV/UV 统计**：页面浏览量和独立访客统计
- **热门页面 Top N**：按访问量排序的热门页面
- **用户访问路径分析**：用户浏览轨迹分析
- **实时数据可视化**：Web 界面展示分析结果

## 🛠️ 技术栈

| 组件 | 技术            | 说明       |
| ---- | --------------- | ---------- |
| 框架 | Spring Boot 3.2 | Web 框架   |
| 模板 | Thymeleaf       | 服务端渲染 |
| 图表 | Chart.js        | 数据可视化 |
| 构建 | Maven           | 项目管理   |

## 📁 项目结构

```
log-analyzer/
├── pom.xml                          # Maven 配置
├── src/main/java/com/example/loganalyzer/
│   ├── LogAnalyzerApplication.java  # 启动类
│   ├── model/
│   │   ├── LogEntry.java            # 日志条目
│   │   ├── PageStats.java           # 页面统计
│   │   └── UserPath.java            # 用户路径
│   ├── parser/
│   │   └── LogParser.java           # 日志解析器
│   ├── mapreduce/
│   │   ├── Mapper.java              # Mapper 接口
│   │   ├── Reducer.java             # Reducer 接口
│   │   ├── MapReduceEngine.java     # MR 引擎
│   │   ├── PvUvMapper.java          # PV/UV Mapper
│   │   ├── PvUvReducer.java         # PV/UV Reducer
│   │   ├── TopNMapper.java          # TopN Mapper
│   │   └── TopNReducer.java         # TopN Reducer
│   ├── service/
│   │   └── AnalyzerService.java     # 分析服务
│   ├── controller/
│   │   ├── AnalyzerController.java  # REST API
│   │   └── WebController.java       # Web 页面
│   └── util/
│       └── LogGenerator.java        # 日志生成器
└── src/main/resources/
    ├── application.yml
    └── templates/index.html          # 可视化页面
```

## 🚀 快速开始

### 1. 运行项目

```bash
cd projects/log-analyzer
mvn spring-boot:run
```

### 2. 访问系统

打开浏览器访问：http://localhost:8080

### 3. 使用流程

1. 点击 **"生成模拟日志"** 生成测试数据（默认 10000 条）
2. 点击 **"执行分析"** 运行 MapReduce 分析
3. 查看可视化结果：
   - 概览统计卡片
   - 热门页面 Top 10 图表
   - PV/UV 分布图
   - 页面统计详情表格

## 📊 API 接口

| 接口                         | 方法 | 说明              |
| ---------------------------- | ---- | ----------------- |
| `/api/logs/generate?count=N` | POST | 生成 N 条模拟日志 |
| `/api/logs/count`            | GET  | 获取日志数量      |
| `/api/stats/overview`        | GET  | 获取概览统计      |
| `/api/stats/pvuv`            | GET  | PV/UV 分析结果    |
| `/api/stats/topn?n=10`       | GET  | TopN 热门页面     |
| `/api/stats/paths`           | GET  | 用户访问路径      |

## 🔄 MapReduce 流程

### PV/UV 统计

```
Input: [LogEntry1, LogEntry2, ...]
         ↓
Map:   <page1, entry1>, <page1, entry2>, <page2, entry3>
         ↓
Shuffle: <page1, [entry1, entry2]>, <page2, [entry3]>
         ↓
Reduce: <page1, PageStats{pv=2, uv=1}>, <page2, PageStats{pv=1, uv=1}>
```

### TopN 分析

```
Input: [LogEntry1, LogEntry2, ...]
         ↓
Map:   <page1, 1>, <page1, 1>, <page2, 1>
         ↓
Shuffle: <page1, [1, 1]>, <page2, [1]>
         ↓
Reduce: <page1, 2>, <page2, 1>
         ↓
Sort:  [<page1, 2>, <page2, 1>]  # 取 Top N
```

## 📚 学习要点

1. **MapReduce 编程模型**

   - Mapper：将输入数据映射为中间 key-value 对
   - Reducer：对相同 key 的 values 进行聚合
   - Shuffle：按 key 分组，为 Reduce 准备数据

2. **日志分析指标**

   - PV (Page View)：页面浏览量
   - UV (Unique Visitor)：独立访客数
   - 响应时间：请求处理耗时
   - 错误率：4xx/5xx 状态码占比

3. **数据可视化**
   - Chart.js 图表库使用
   - 前后端分离 REST API 设计

## 🎓 扩展练习

1. 添加按时间维度的 PV/UV 趋势分析
2. 实现用户访问路径的可视化（桑基图）
3. 添加日志文件导入功能
4. 实现分布式 MapReduce（多线程模拟）
