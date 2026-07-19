-- V008: 创建留言回复记录表（支持多轮对话）
CREATE TABLE IF NOT EXISTS message_reply (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '留言ID',
    user_id BIGINT DEFAULT NULL COMMENT '回复用户ID（C端用户）',
    user_name VARCHAR(50) NOT NULL COMMENT '回复人姓名',
    user_type VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '回复人类型: ADMIN-管理员, USER-群众',
    content TEXT NOT NULL COMMENT '回复内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    INDEX idx_message_id (message_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='留言回复记录（多轮对话）';
