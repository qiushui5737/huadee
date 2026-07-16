-- =====================================================
-- 政府网站集约化平台 数据库初始化脚本 (单体模式)
-- MySQL 9.3.0 - 单库 gov_platform
-- =====================================================

CREATE DATABASE IF NOT EXISTS gov_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gov_platform;

-- =====================================================
-- AI智能服务 (A组: A1-A5)
-- =====================================================

-- 敏感词库(A4)
CREATE TABLE IF NOT EXISTS sensitive_word (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    word        VARCHAR(100) NOT NULL COMMENT '敏感词',
    category    VARCHAR(50)  DEFAULT NULL COMMENT '分类',
    level       TINYINT      DEFAULT 1 COMMENT '级别 1低 2中 3高',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0,
    UNIQUE KEY uk_word (word)
) ENGINE=InnoDB COMMENT='敏感词库';

-- AI会话记录(A5)
CREATE TABLE IF NOT EXISTS chat_session (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    user_id     BIGINT       DEFAULT NULL COMMENT '用户ID',
    question    TEXT         NOT NULL COMMENT '用户提问',
    answer      TEXT         COMMENT 'AI回答',
    sources     TEXT         COMMENT '知识库来源(JSON)',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session (session_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB COMMENT='AI问答会话记录';

-- 知识库文档(A2)
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    content     LONGTEXT,
    source      VARCHAR(200) COMMENT '来源',
    dept_code   VARCHAR(50)  COMMENT '部门编码',
    status      TINYINT      DEFAULT 1 COMMENT '0待索引 1已索引',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB COMMENT='政务知识库文档';

-- =====================================================
-- 办事服务平台 (B组: B1-B5)
-- =====================================================

CREATE TABLE IF NOT EXISTS service_item (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code   VARCHAR(50)  NOT NULL COMMENT '事项编码',
    item_name   VARCHAR(200) NOT NULL COMMENT '事项名称',
    category    VARCHAR(50)  COMMENT '分类',
    dept_code   VARCHAR(50)  COMMENT '承办部门',
    description TEXT         COMMENT '事项描述',
    form_schema TEXT         COMMENT '表单JSON Schema',
    status      TINYINT      DEFAULT 1 COMMENT '0下线 1上线',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0,
    UNIQUE KEY uk_code (item_code)
) ENGINE=InnoDB COMMENT='办事事项目录';

CREATE TABLE IF NOT EXISTS service_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    accept_no   VARCHAR(50)  NOT NULL COMMENT '受理号',
    item_id     BIGINT       NOT NULL COMMENT '事项ID',
    user_id     BIGINT       COMMENT '申请人ID',
    user_name   VARCHAR(50)  COMMENT '申请人姓名',
    form_data   TEXT         COMMENT '表单数据(JSON)',
    status      VARCHAR(20)  DEFAULT '受理中' COMMENT '状态',
    submit_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME     DEFAULT NULL,
    UNIQUE KEY uk_accept (accept_no)
) ENGINE=InnoDB COMMENT='办件记录';

-- =====================================================
-- 政民互动与公开 (C组: C1-C5)
-- =====================================================

CREATE TABLE IF NOT EXISTS message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       COMMENT '群众ID',
    user_name   VARCHAR(50)  COMMENT '群众姓名',
    title       VARCHAR(200) NOT NULL COMMENT '标题',
    content     TEXT         NOT NULL COMMENT '内容',
    dept_code   VARCHAR(50)  COMMENT '分派部门',
    status      VARCHAR(20)  DEFAULT '待分派' COMMENT '状态: 待分派/已分派/已回复/已办结',
    reply_content TEXT       COMMENT '回复内容',
    reply_by    VARCHAR(50)  COMMENT '回复人',
    reply_time  DATETIME     DEFAULT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by   BIGINT       DEFAULT 0 COMMENT '创建人',
    update_by   BIGINT       DEFAULT 0 COMMENT '更新人',
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB COMMENT='政民互动留言';

CREATE TABLE IF NOT EXISTS disclosure_apply (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no    VARCHAR(50)  NOT NULL COMMENT '申请编号',
    applicant   VARCHAR(50)  NOT NULL COMMENT '申请人',
    id_card     VARCHAR(20)  COMMENT '身份证号',
    phone       VARCHAR(20)  COMMENT '联系电话',
    content     TEXT         NOT NULL COMMENT '申请内容',
    purpose     VARCHAR(500) COMMENT '用途说明',
    status      VARCHAR(20)  DEFAULT '已受理' COMMENT '状态: 已受理/审核中/已答复/已驳回',
    deadline    DATE         COMMENT '答复期限(15个工作日)',
    reply_content TEXT       COMMENT '答复内容',
    reply_by    VARCHAR(50)  COMMENT '答复人',
    reply_time  DATETIME     DEFAULT NULL,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_apply_no (apply_no)
) ENGINE=InnoDB COMMENT='依申请公开记录';

-- =====================================================
-- CMS部门集约化 (D组: D1-D5)
-- =====================================================

CREATE TABLE IF NOT EXISTS cms_site (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_code   VARCHAR(50)  NOT NULL COMMENT '子站编码',
    site_name   VARCHAR(100) NOT NULL COMMENT '子站名称',
    dept_code   VARCHAR(50)  COMMENT '部门编码',
    template    VARCHAR(50)  DEFAULT 'default' COMMENT '模板',
    status      TINYINT      DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (site_code)
) ENGINE=InnoDB COMMENT='部门子站';

CREATE TABLE IF NOT EXISTS cms_content (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_code   VARCHAR(50)  NOT NULL COMMENT '所属子站',
    title       VARCHAR(300) NOT NULL COMMENT '标题',
    content     LONGTEXT     COMMENT '正文',
    category    VARCHAR(50)  COMMENT '栏目: 通知/政策/公告/新闻',
    author      VARCHAR(50)  COMMENT '发布人',
    status      VARCHAR(20)  DEFAULT '待审核' COMMENT '状态: 待审核/初审通过/终审通过/已发布',
    publish_time DATETIME    DEFAULT NULL COMMENT '发布时间',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB COMMENT='CMS内容';

CREATE TABLE IF NOT EXISTS disclosure_catalog (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT       DEFAULT 0 COMMENT '父节点ID',
    name        VARCHAR(100) NOT NULL COMMENT '目录名称',
    code        VARCHAR(50)  COMMENT '目录编码',
    level       TINYINT      DEFAULT 1 COMMENT '层级',
    sort_order  INT          DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB COMMENT='信息公开目录';

-- =====================================================
-- 统计与公共服务 (E组: E1-E5)
-- =====================================================

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL COMMENT '用户名/工号',
    password    VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    real_name   VARCHAR(50)  COMMENT '姓名',
    phone       VARCHAR(20)  COMMENT '手机号',
    dept_code   VARCHAR(50)  COMMENT '所属部门',
    status      TINYINT      DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0,
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code   VARCHAR(50)  NOT NULL,
    role_name   VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (role_code)
) ENGINE=InnoDB COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联';

CREATE TABLE IF NOT EXISTS performance_metric (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code   VARCHAR(50)  NOT NULL COMMENT '部门编码',
    metric_code VARCHAR(50)  NOT NULL COMMENT '指标编码',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    target_value DECIMAL(10,2) COMMENT '目标值',
    actual_value DECIMAL(10,2) COMMENT '实际值',
    score       DECIMAL(5,2) COMMENT '得分',
    period      VARCHAR(10)  COMMENT '考核周期: 2026-Q3',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_metric (dept_code, metric_code, period)
) ENGINE=InnoDB COMMENT='绩效指标';

CREATE TABLE IF NOT EXISTS sys_message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       COMMENT '接收人ID',
    type        VARCHAR(20)  DEFAULT 'system' COMMENT '类型: system/sms/wechat',
    title       VARCHAR(200) COMMENT '标题',
    content     TEXT         COMMENT '内容',
    is_read     TINYINT      DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统消息';

-- =====================================================
-- 初始化数据
-- =====================================================
INSERT INTO sys_user (username, password, real_name, dept_code) VALUES
  ('admin', '$2b$10$BjPh4TqxXwz4bHenk/t8x.qqyyZQ7ILlsQZwlq04itIU3hlzdr2jq', '系统管理员', 'ADMIN'),
  ('edu_user', '$2b$10$BjPh4TqxXwz4bHenk/t8x.qqyyZQ7ILlsQZwlq04itIU3hlzdr2jq', '教育部专员', 'EDU'),
  ('hea_user', '$2b$10$BjPh4TqxXwz4bHenk/t8x.qqyyZQ7ILlsQZwlq04itIU3hlzdr2jq', '卫健委专员', 'HEA');

INSERT INTO sys_role (role_code, role_name) VALUES
  ('ADMIN', '系统管理员'),
  ('DEPT_ADMIN', '部门管理员'),
  ('OPERATOR', '操作员');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2), (3, 2);

INSERT INTO performance_metric (dept_code, metric_code, metric_name, target_value, actual_value, score, period) VALUES
  ('EDU', 'RESPONSE_TIME', '留言平均响应时间(小时)', 24, 18.5, 92.5, '2026-Q3'),
  ('HEA', 'RESPONSE_TIME', '留言平均响应时间(小时)', 24, 26.2, 89.3, '2026-Q3'),
  ('EDU', 'SERVICE_RATE', '办事办结率', 95, 96.8, 96.8, '2026-Q3');

INSERT INTO sensitive_word (word, category, level) VALUES
  ('测试敏感词1', '政治', 3),
  ('测试敏感词2', '其他', 1);
