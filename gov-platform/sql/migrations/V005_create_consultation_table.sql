-- =====================================================
-- V005: 创建咨询表 consultation - 我要咨询功能
-- 作者: dev-interaction
-- 日期: 2026-07-15
-- =====================================================

CREATE TABLE IF NOT EXISTS consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    consult_no VARCHAR(30) NOT NULL UNIQUE COMMENT '咨询单号',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号码',
    telephone VARCHAR(20) DEFAULT NULL COMMENT '电话号码',
    email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    title VARCHAR(100) NOT NULL COMMENT '咨询主题',
    city VARCHAR(50) NOT NULL COMMENT '咨询市州',
    district VARCHAR(50) DEFAULT NULL COMMENT '事件发生地-区',
    street VARCHAR(50) DEFAULT NULL COMMENT '事件发生地-街道',
    address VARCHAR(200) DEFAULT NULL COMMENT '事件发生具体地址',
    attachment LONGTEXT DEFAULT NULL COMMENT '附件(JSON格式,含base64图片数据)',
    content TEXT NOT NULL COMMENT '咨询内容',
    is_public TINYINT(1) DEFAULT 1 COMMENT '是否愿意公开: 1是 0否',
    is_confidential TINYINT(1) DEFAULT 0 COMMENT '是否保密: 1是 0否',
    status VARCHAR(20) DEFAULT '待受理' COMMENT '状态: 待受理/处理中/已答复/已办结',
    reply_content TEXT DEFAULT NULL COMMENT '答复内容',
    reply_by VARCHAR(50) DEFAULT NULL COMMENT '答复人',
    reply_time DATETIME DEFAULT NULL COMMENT '答复时间',
    deadline DATE DEFAULT NULL COMMENT '答复期限',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT 0 COMMENT '创建人',
    update_by BIGINT DEFAULT 0 COMMENT '更新人',
    deleted INT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    INDEX idx_consult_no (consult_no),
    INDEX idx_status (status),
    INDEX idx_city (city),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政民互动-咨询表';

