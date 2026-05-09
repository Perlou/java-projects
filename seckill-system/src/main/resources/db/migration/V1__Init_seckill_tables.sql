-- 秒杀系统数据库初始化脚本
-- V1__Init_seckill_tables.sql

-- 秒杀商品表
CREATE TABLE IF NOT EXISTS seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀商品ID',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    goods_img VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
    original_price DECIMAL(10, 2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价',
    stock_count INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    start_time DATETIME NOT NULL COMMENT '秒杀开始时间',
    end_time DATETIME NOT NULL COMMENT '秒杀结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀商品表';

-- 秒杀订单表
CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '秒杀商品ID',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    seckill_price DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-排队中 1-成功 2-失败',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_goods (user_id, goods_id) COMMENT '一人一单',
    INDEX idx_user_id (user_id),
    INDEX idx_goods_id (goods_id),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '秒杀订单表';

-- 插入测试数据
INSERT INTO
    seckill_goods (
        goods_name,
        goods_img,
        original_price,
        seckill_price,
        stock_count,
        start_time,
        end_time,
        status
    )
VALUES (
        'iPhone 15 Pro Max 256GB',
        '/images/iphone15.jpg',
        9999.00,
        7999.00,
        100,
        NOW(),
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        1
    ),
    (
        'MacBook Pro 14寸 M3',
        '/images/macbook.jpg',
        14999.00,
        12999.00,
        50,
        NOW(),
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        1
    ),
    (
        'AirPods Pro 2',
        '/images/airpods.jpg',
        1899.00,
        1499.00,
        200,
        NOW(),
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        1
    ),
    (
        'Apple Watch Ultra 2',
        '/images/watch.jpg',
        6499.00,
        5499.00,
        30,
        NOW(),
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        1
    ),
    (
        'iPad Pro 12.9寸',
        '/images/ipad.jpg',
        8999.00,
        7499.00,
        80,
        NOW(),
        DATE_ADD(NOW(), INTERVAL 7 DAY),
        1
    );