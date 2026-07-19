-- V012: 创建意见征集表 + 意见提交表
CREATE TABLE IF NOT EXISTS collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '征集主题',
    description TEXT COMMENT '征集说明',
    dept_name VARCHAR(100) DEFAULT NULL COMMENT '征集单位',
    contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    attachment_name VARCHAR(200) DEFAULT NULL COMMENT '附件名称',
    attachment_url VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    start_date DATE NOT NULL COMMENT '征集开始日期',
    end_date DATE NOT NULL COMMENT '征集结束日期',
    status VARCHAR(20) DEFAULT '进行中' COMMENT '状态: 进行中/已结束',
    feedback TEXT DEFAULT NULL COMMENT '结果反馈内容',
    feedback_time DATETIME DEFAULT NULL COMMENT '反馈时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见征集表';

CREATE TABLE IF NOT EXISTS collection_opinion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id BIGINT NOT NULL COMMENT '关联征集ID',
    user_id BIGINT DEFAULT NULL COMMENT '提交用户ID',
    title VARCHAR(200) NOT NULL COMMENT '意见标题',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号码',
    id_card VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    address VARCHAR(200) DEFAULT NULL COMMENT '通信地址',
    content TEXT NOT NULL COMMENT '留言内容',
    status VARCHAR(20) DEFAULT '待处理' COMMENT '状态: 待处理/已回复',
    reply_content TEXT DEFAULT NULL COMMENT '回复内容',
    reply_by VARCHAR(50) DEFAULT NULL COMMENT '回复人',
    reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    INDEX idx_collection_id (collection_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见征集-意见提交表';

