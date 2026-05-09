-- 示例数仓表元数据

-- ODS 层
INSERT INTO
    table_metadata (
        table_name,
        table_comment,
        dw_layer,
        database_name,
        owner
    )
VALUES (
        'ods_mysql_orders',
        '订单原始表',
        'ODS',
        'ods',
        'data_team'
    ),
    (
        'ods_mysql_users',
        '用户原始表',
        'ODS',
        'ods',
        'data_team'
    ),
    (
        'ods_mysql_products',
        '商品原始表',
        'ODS',
        'ods',
        'data_team'
    ),
    (
        'ods_kafka_user_behavior',
        '用户行为日志',
        'ODS',
        'ods',
        'data_team'
    );

-- DIM 维度层
INSERT INTO
    table_metadata (
        table_name,
        table_comment,
        dw_layer,
        database_name,
        owner
    )
VALUES (
        'dim_user',
        '用户维度表',
        'DIM',
        'dim',
        'data_team'
    ),
    (
        'dim_product',
        '商品维度表',
        'DIM',
        'dim',
        'data_team'
    ),
    (
        'dim_date',
        '日期维度表',
        'DIM',
        'dim',
        'data_team'
    );

-- DWD 明细层
INSERT INTO
    table_metadata (
        table_name,
        table_comment,
        dw_layer,
        database_name,
        owner
    )
VALUES (
        'dwd_trade_order_detail',
        '订单明细宽表',
        'DWD',
        'dwd',
        'data_team'
    ),
    (
        'dwd_user_behavior_event',
        '用户行为事件表',
        'DWD',
        'dwd',
        'data_team'
    );

-- DWS 汇总层
INSERT INTO
    table_metadata (
        table_name,
        table_comment,
        dw_layer,
        database_name,
        owner
    )
VALUES (
        'dws_trade_user_order_1d',
        '用户交易日汇总',
        'DWS',
        'dws',
        'data_team'
    ),
    (
        'dws_user_behavior_1d',
        '用户行为日汇总',
        'DWS',
        'dws',
        'data_team'
    );

-- ADS 应用层
INSERT INTO
    table_metadata (
        table_name,
        table_comment,
        dw_layer,
        database_name,
        owner
    )
VALUES (
        'ads_report_sales_summary',
        '销售汇总报表',
        'ADS',
        'ads',
        'data_team'
    ),
    (
        'ads_dashboard_user_portrait',
        '用户画像看板',
        'ADS',
        'ads',
        'data_team'
    );

-- 列元数据示例
INSERT INTO
    column_metadata (
        table_id,
        column_name,
        column_type,
        column_comment,
        is_primary_key,
        ordinal_position
    )
VALUES (
        1,
        'id',
        'BIGINT',
        '订单ID',
        TRUE,
        1
    ),
    (
        1,
        'order_no',
        'STRING',
        '订单号',
        FALSE,
        2
    ),
    (
        1,
        'user_id',
        'BIGINT',
        '用户ID',
        FALSE,
        3
    ),
    (
        1,
        'product_id',
        'BIGINT',
        '商品ID',
        FALSE,
        4
    ),
    (
        1,
        'amount',
        'DECIMAL',
        '订单金额',
        FALSE,
        5
    ),
    (
        1,
        'status',
        'INT',
        '订单状态',
        FALSE,
        6
    ),
    (
        1,
        'create_time',
        'TIMESTAMP',
        '创建时间',
        FALSE,
        7
    ),
    (
        1,
        'dt',
        'STRING',
        '分区字段',
        FALSE,
        8
    );

INSERT INTO
    column_metadata (
        table_id,
        column_name,
        column_type,
        column_comment,
        is_partition,
        ordinal_position
    )
VALUES (
        8,
        'order_id',
        'BIGINT',
        '订单ID',
        FALSE,
        1
    ),
    (
        8,
        'user_id',
        'BIGINT',
        '用户ID',
        FALSE,
        2
    ),
    (
        8,
        'user_name',
        'STRING',
        '用户名',
        FALSE,
        3
    ),
    (
        8,
        'product_id',
        'BIGINT',
        '商品ID',
        FALSE,
        4
    ),
    (
        8,
        'product_name',
        'STRING',
        '商品名',
        FALSE,
        5
    ),
    (
        8,
        'order_amount',
        'DECIMAL',
        '订单金额',
        FALSE,
        6
    ),
    (
        8,
        'dt',
        'STRING',
        '日期分区',
        TRUE,
        7
    );

-- 数据质量规则
INSERT INTO
    quality_rule (
        rule_name,
        rule_type,
        table_id,
        column_name,
        rule_expression,
        threshold
    )
VALUES (
        '订单ID非空检查',
        'COMPLETENESS',
        1,
        'id',
        'SELECT COUNT(*) FROM ods_mysql_orders WHERE id IS NULL',
        100
    ),
    (
        '订单ID唯一性检查',
        'UNIQUENESS',
        1,
        'id',
        'SELECT COUNT(*) - COUNT(DISTINCT id) FROM ods_mysql_orders',
        100
    ),
    (
        '订单金额有效性检查',
        'VALIDITY',
        1,
        'amount',
        'SELECT COUNT(*) FROM ods_mysql_orders WHERE amount < 0',
        100
    ),
    (
        '用户ID非空检查',
        'COMPLETENESS',
        8,
        'user_id',
        'SELECT COUNT(*) FROM dwd_trade_order_detail WHERE user_id IS NULL',
        100
    ),
    (
        '订单状态枚举检查',
        'VALIDITY',
        1,
        'status',
        'SELECT COUNT(*) FROM ods_mysql_orders WHERE status NOT IN (0,1,2,3,4)',
        100
    );

-- 数据血缘
INSERT INTO
    lineage_edge (
        source_table_id,
        target_table_id,
        source_column,
        target_column,
        transformation
    )
VALUES
    -- ODS → DIM
    (
        2,
        5,
        'user_id,user_name',
        'user_id,user_name',
        'SCD Type 2'
    ),
    (
        3,
        6,
        'product_id,product_name',
        'product_id,product_name',
        'SCD Type 1'
    ),
    -- ODS → DWD
    (
        1,
        8,
        'id',
        'order_id',
        'DIRECT'
    ),
    (
        1,
        8,
        'amount',
        'order_amount',
        'DIRECT'
    ),
    (
        5,
        8,
        'user_name',
        'user_name',
        'JOIN'
    ),
    (
        6,
        8,
        'product_name',
        'product_name',
        'JOIN'
    ),
    -- DWD → DWS
    (
        8,
        10,
        'user_id,order_amount',
        'user_id,order_amount_1d',
        'SUM'
    ),
    -- DWS → ADS
    (
        10,
        12,
        'user_id',
        'user_id',
        'AGG'
    );

-- ETL 任务
INSERT INTO
    etl_task (
        task_name,
        task_type,
        source_tables,
        target_table,
        sql_content,
        schedule_cron,
        status
    )
VALUES (
        'ods_to_dim_user',
        'SPARK',
        'ods_mysql_users',
        'dim_user',
        'INSERT OVERWRITE TABLE dim_user SELECT * FROM ods_mysql_users',
        '0 1 * * *',
        'STOPPED'
    ),
    (
        'ods_to_dwd_order',
        'SPARK',
        'ods_mysql_orders,dim_user,dim_product',
        'dwd_trade_order_detail',
        'INSERT OVERWRITE TABLE dwd_trade_order_detail SELECT o.*, u.user_name, p.product_name FROM ods_mysql_orders o JOIN dim_user u ON o.user_id = u.user_id JOIN dim_product p ON o.product_id = p.product_id',
        '0 2 * * *',
        'STOPPED'
    ),
    (
        'dwd_to_dws_order',
        'SPARK',
        'dwd_trade_order_detail',
        'dws_trade_user_order_1d',
        'INSERT OVERWRITE TABLE dws_trade_user_order_1d SELECT user_id, COUNT(*) AS order_count, SUM(order_amount) AS order_amount FROM dwd_trade_order_detail GROUP BY user_id',
        '0 3 * * *',
        'STOPPED'
    );