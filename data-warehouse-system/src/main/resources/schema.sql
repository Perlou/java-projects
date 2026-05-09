-- 数仓表元数据
CREATE TABLE IF NOT EXISTS table_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(200) NOT NULL,
    table_comment VARCHAR(500),
    dw_layer VARCHAR(20) NOT NULL, -- ODS, DWD, DWS, ADS, DIM
    database_name VARCHAR(100),
    owner VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 列元数据
CREATE TABLE IF NOT EXISTS column_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_id BIGINT NOT NULL,
    column_name VARCHAR(200) NOT NULL,
    column_type VARCHAR(50) NOT NULL,
    column_comment VARCHAR(500),
    is_partition BOOLEAN DEFAULT FALSE,
    is_primary_key BOOLEAN DEFAULT FALSE,
    ordinal_position INT,
    FOREIGN KEY (table_id) REFERENCES table_metadata (id)
);

-- 数据质量规则
CREATE TABLE IF NOT EXISTS quality_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- COMPLETENESS, UNIQUENESS, VALIDITY, CONSISTENCY
    table_id BIGINT,
    column_name VARCHAR(200),
    rule_expression VARCHAR(1000), -- SQL 表达式
    threshold DOUBLE DEFAULT 100.0, -- 通过率阈值
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES table_metadata (id)
);

-- 数据质量检测结果
CREATE TABLE IF NOT EXISTS quality_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    table_id BIGINT NOT NULL,
    check_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_count BIGINT,
    pass_count BIGINT,
    fail_count BIGINT,
    pass_rate DOUBLE,
    status VARCHAR(20), -- PASS, FAIL, ERROR
    error_message VARCHAR(1000),
    FOREIGN KEY (rule_id) REFERENCES quality_rule (id),
    FOREIGN KEY (table_id) REFERENCES table_metadata (id)
);

-- 数据血缘边
CREATE TABLE IF NOT EXISTS lineage_edge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_table_id BIGINT NOT NULL,
    target_table_id BIGINT NOT NULL,
    source_column VARCHAR(200),
    target_column VARCHAR(200),
    transformation VARCHAR(500), -- 转换逻辑
    etl_task_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (source_table_id) REFERENCES table_metadata (id),
    FOREIGN KEY (target_table_id) REFERENCES table_metadata (id)
);

-- ETL 任务
CREATE TABLE IF NOT EXISTS etl_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL,
    task_type VARCHAR(50), -- SPARK, HIVE, FLINK
    source_tables VARCHAR(1000), -- 源表，逗号分隔
    target_table VARCHAR(200),
    sql_content TEXT,
    schedule_cron VARCHAR(50),
    status VARCHAR(20) DEFAULT 'STOPPED', -- RUNNING, STOPPED, FAILED
    last_run_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);