package com.example.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业级数据仓库管理系统
 * 
 * 模拟数仓核心功能：
 * 
 * 【数仓分层管理】
 * - ODS: 原始数据层
 * - DWD: 明细数据层
 * - DWS: 服务数据层
 * - ADS: 应用数据层
 * - DIM: 维度表层
 * 
 * 【数据质量检测】
 * - 完整性检查 (Completeness)
 * - 唯一性检查 (Uniqueness)
 * - 有效性检查 (Validity)
 * - 一致性检查 (Consistency)
 * 
 * 【数据血缘追踪】
 * - SQL 解析提取血缘
 * - 表级/列级血缘
 * - 上下游影响分析
 * 
 * 【ETL 任务管理】
 * - 任务依赖
 * - 执行状态
 * - 模拟执行
 * 
 * @author Java Course
 * @since Phase 24
 */
@SpringBootApplication
public class DataWarehouseApplication {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          Phase 24: 企业级数据仓库管理系统                ║");
        System.out.println("║          Data Warehouse Management System                ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("启动中...");
        System.out.println();

        SpringApplication.run(DataWarehouseApplication.class, args);

        System.out.println();
        System.out.println("【系统已启动】");
        System.out.println("  • 应用地址: http://localhost:8082");
        System.out.println("  • Swagger UI: http://localhost:8082/swagger-ui.html");
        System.out.println("  • H2 控制台: http://localhost:8082/h2-console");
        System.out.println();
        System.out.println("【API 端点】");
        System.out.println("  • 表管理: /api/tables");
        System.out.println("  • 数据质量: /api/quality");
        System.out.println("  • 数据血缘: /api/lineage");
        System.out.println("  • ETL 任务: /api/etl");
    }
}
