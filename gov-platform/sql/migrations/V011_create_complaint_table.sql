-- V011: 创建投诉表
CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '提交用户ID',
    complaint_no VARCHAR(32) NOT NULL COMMENT '投诉单号',
    title VARCHAR(100) NOT NULL COMMENT '投诉标题',
    -- 投诉单位（三级层级）
    complaint_unit_l1 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-一级(市州)',
    complaint_unit_l2 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-二级(部门)',
    complaint_unit_l3 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-三级(子部门)',
    -- 问题类型（三级层级）
    problem_type_l1 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-一级',
    problem_type_l2 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-二级',
    problem_type_l3 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-三级',
    -- 投诉人信息
    real_name VARCHAR(50) NOT NULL COMMENT '投诉人姓名',
    id_card VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    email VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    address VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    -- 事件发生地
    province VARCHAR(50) DEFAULT NULL COMMENT '省份',
    city VARCHAR(50) DEFAULT NULL COMMENT '城市',
    district VARCHAR(50) DEFAULT NULL COMMENT '区县',
    detail_address VARCHAR(100) DEFAULT NULL COMMENT '具体地址',
    -- 内容
    content TEXT NOT NULL COMMENT '投诉内容',
    attachment TEXT DEFAULT NULL COMMENT '附件(JSON)',
    is_public TINYINT(1) DEFAULT 1 COMMENT '是否公开: 1-是, 0-否',
    is_secret TINYINT(1) DEFAULT 0 COMMENT '是否保密: 1-是, 0-否',
    -- 状态流转
    status VARCHAR(20) DEFAULT '待受理' COMMENT '状态: 待受理/处理中/已答复/已办结',
    reply_content TEXT DEFAULT NULL COMMENT '答复内容',
    reply_by VARCHAR(50) DEFAULT NULL COMMENT '答复人',
    reply_time DATETIME DEFAULT NULL COMMENT '答复时间',
    deadline DATE DEFAULT NULL COMMENT '答复期限',
    -- BaseEntity字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    UNIQUE INDEX idx_complaint_no (complaint_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉表';

