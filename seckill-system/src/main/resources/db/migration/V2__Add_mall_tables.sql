-- Phase 11: 商城系统扩展表

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status INT DEFAULT 1 COMMENT '0:禁用 1:正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品表 (常规商品，非秒杀)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    price DECIMAL(10, 2) NOT NULL,
    stock INT DEFAULT 0,
    status INT DEFAULT 1 COMMENT '0:下架 1:上架',
    version INT DEFAULT 0 COMMENT '乐观锁',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_status (status)
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status INT DEFAULT 0 COMMENT '0:待支付 1:已支付 2:已发货 3:已完成 4:已取消',
    payment_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    INDEX idx_order_id (order_id)
);

-- 初始数据：管理员用户
INSERT INTO
    users (
        username,
        password,
        nickname,
        status
    )
VALUES ('admin', '123456', '管理员', 1),
    ('demo', '123456', '测试用户', 1);

-- 初始数据：商品分类
INSERT INTO
    categories (
        name,
        parent_id,
        level,
        sort_order
    )
VALUES ('数码产品', 0, 1, 1),
    ('手机', 1, 2, 1),
    ('电脑', 1, 2, 2),
    ('服装', 0, 1, 2);

-- 初始数据：商品
INSERT INTO
    products (
        category_id,
        name,
        description,
        price,
        stock,
        status,
        version
    )
VALUES (
        2,
        'iPhone 15',
        'Apple iPhone 15 128GB',
        5999.00,
        100,
        1,
        0
    ),
    (
        2,
        'iPhone 15 Pro',
        'Apple iPhone 15 Pro 256GB',
        8999.00,
        50,
        1,
        0
    ),
    (
        3,
        'MacBook Pro 14',
        'Apple M3 Pro 芯片',
        14999.00,
        30,
        1,
        0
    ),
    (
        3,
        'MacBook Air 13',
        'Apple M2 芯片',
        7999.00,
        80,
        1,
        0
    ),
    (
        4,
        '休闲T恤',
        '纯棉短袖T恤',
        99.00,
        500,
        1,
        0
    );