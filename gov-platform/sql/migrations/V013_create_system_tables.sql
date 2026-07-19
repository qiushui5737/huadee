-- =====================================================
-- V013: 补充遗漏的系统基础表
-- 作者: dev-interaction
-- 日期: 2026-07-18
-- 说明: 以下表在数据库中存在但未写入 migrations 目录
-- =====================================================

USE gov_platform;

-- 系统部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL COMMENT '部门编码',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_sys_dept_code (dept_code)
) ENGINE=InnoDB COMMENT='系统部门表';

-- 系统权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    perm_type VARCHAR(20) DEFAULT 'menu' COMMENT '权限类型: menu/button/api',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_permission_code (perm_code)
) ENGINE=InnoDB COMMENT='系统权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB COMMENT='角色权限关联表';

-- 通知模板表
CREATE TABLE IF NOT EXISTS notification_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    channel VARCHAR(30) NOT NULL COMMENT '通知渠道: sms/email/wechat/site',
    content TEXT NOT NULL COMMENT '模板内容',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notification_template_code (template_code)
) ENGINE=InnoDB COMMENT='通知模板表';

-- 访问日志表
CREATE TABLE IF NOT EXISTS access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    session_id VARCHAR(80) DEFAULT NULL COMMENT '会话ID',
    page_url VARCHAR(500) DEFAULT NULL COMMENT '页面URL',
    event_type VARCHAR(50) DEFAULT NULL COMMENT '事件类型',
    event_data JSON DEFAULT NULL COMMENT '事件数据',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_access_log_time (create_time)
) ENGINE=InnoDB COMMENT='访问日志表';

-- AI对话会话表
CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    title VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    memory_summary TEXT DEFAULT NULL COMMENT '记忆摘要',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ai_conversation_user (user_id)
) ENGINE=InnoDB COMMENT='AI对话会话表';

-- 向量嵌入表
CREATE TABLE IF NOT EXISTS vector_embedding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    biz_id BIGINT NOT NULL COMMENT '业务ID',
    chunk_no INT DEFAULT 0 COMMENT '分片序号',
    chunk_text TEXT NOT NULL COMMENT '分片文本',
    embedding LONGTEXT DEFAULT NULL COMMENT '向量数据',
    model_name VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_vector_embedding_biz (biz_type, biz_id)
) ENGINE=InnoDB COMMENT='向量嵌入表';

-- 搜索索引任务表
CREATE TABLE IF NOT EXISTS search_index_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    biz_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    biz_id BIGINT NOT NULL COMMENT '业务ID',
    operation VARCHAR(20) NOT NULL COMMENT '操作: create/update/delete',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/processing/done/failed',
    error_msg TEXT DEFAULT NULL COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    INDEX idx_search_index_task_status (status)
) ENGINE=InnoDB COMMENT='搜索索引任务表';

-- ETL任务表
CREATE TABLE IF NOT EXISTS etl_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_code VARCHAR(50) NOT NULL COMMENT '任务编码',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    cron_expr VARCHAR(50) DEFAULT NULL COMMENT 'Cron表达式',
    last_run_time DATETIME DEFAULT NULL COMMENT '上次执行时间',
    last_status VARCHAR(30) DEFAULT NULL COMMENT '上次执行状态',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用: 0否 1是',
    UNIQUE KEY uk_etl_job_code (job_code)
) ENGINE=InnoDB COMMENT='ETL任务表';

-- 报表统计表
CREATE TABLE IF NOT EXISTS report_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_type VARCHAR(50) NOT NULL COMMENT '统计类型',
    stat_key VARCHAR(80) NOT NULL COMMENT '统计项',
    stat_value DECIMAL(18,2) DEFAULT 0.00 COMMENT '统计值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_stat (stat_date, stat_type, stat_key)
) ENGINE=InnoDB COMMENT='报表统计表';
