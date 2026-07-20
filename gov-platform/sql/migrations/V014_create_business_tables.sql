-- =====================================================
-- V014: 补充遗漏的业务功能表（办事服务/CMS/公开/互动）
-- 作者: dev-interaction
-- 日期: 2026-07-18
-- 说明: 以下表在数据库中存在但未写入 migrations 目录
-- =====================================================

USE gov_platform;

-- 办事服务分类表
CREATE TABLE IF NOT EXISTS service_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    UNIQUE KEY uk_service_category_code (category_code)
) ENGINE=InnoDB COMMENT='办事服务分类表';

-- 表单定义表
CREATE TABLE IF NOT EXISTS form_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_code VARCHAR(50) NOT NULL COMMENT '表单编码',
    form_name VARCHAR(100) NOT NULL COMMENT '表单名称',
    item_id BIGINT DEFAULT NULL COMMENT '关联事项ID',
    schema_json JSON NOT NULL COMMENT '表单Schema(JSON)',
    ui_schema JSON DEFAULT NULL COMMENT 'UI布局Schema',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_form_definition_code_version (form_code, version)
) ENGINE=InnoDB COMMENT='表单定义表';

-- 工作流定义表
CREATE TABLE IF NOT EXISTS workflow_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    flow_key VARCHAR(80) NOT NULL COMMENT '流程标识',
    flow_name VARCHAR(100) NOT NULL COMMENT '流程名称',
    bpmn_xml LONGTEXT DEFAULT NULL COMMENT 'BPMN流程定义XML',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_workflow_definition_key_version (flow_key, version)
) ENGINE=InnoDB COMMENT='工作流定义表';

-- 审批任务表
CREATE TABLE IF NOT EXISTS approval_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accept_no VARCHAR(50) NOT NULL COMMENT '受理号',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    assignee_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    assignee_name VARCHAR(50) DEFAULT NULL COMMENT '处理人姓名',
    action VARCHAR(30) DEFAULT NULL COMMENT '操作: approve/reject/transfer',
    opinion VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    status VARCHAR(30) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    INDEX idx_approval_task_accept (accept_no)
) ENGINE=InnoDB COMMENT='审批任务表';

-- 支付订单表
CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    accept_no VARCHAR(50) NOT NULL COMMENT '受理号',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    pay_channel VARCHAR(30) DEFAULT NULL COMMENT '支付渠道',
    pay_status VARCHAR(30) DEFAULT 'unpaid' COMMENT '支付状态: unpaid/paid/refunded',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_order_no (order_no)
) ENGINE=InnoDB COMMENT='支付订单表';

-- 电子证照表
CREATE TABLE IF NOT EXISTS electronic_license (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_no VARCHAR(80) NOT NULL COMMENT '证照编号',
    accept_no VARCHAR(50) NOT NULL COMMENT '受理号',
    license_name VARCHAR(100) NOT NULL COMMENT '证照名称',
    holder_name VARCHAR(50) DEFAULT NULL COMMENT '持证人',
    issue_dept VARCHAR(50) DEFAULT NULL COMMENT '发证机关',
    issue_time DATETIME DEFAULT NULL COMMENT '发证时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    file_url VARCHAR(300) DEFAULT NULL COMMENT '证照文件URL',
    status VARCHAR(30) DEFAULT 'valid' COMMENT '状态: valid/expired/revoked',
    UNIQUE KEY uk_electronic_license_no (license_no)
) ENGINE=InnoDB COMMENT='电子证照表';

-- CMS栏目表
CREATE TABLE IF NOT EXISTS cms_channel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    site_code VARCHAR(50) NOT NULL COMMENT '所属子站编码',
    channel_code VARCHAR(50) NOT NULL COMMENT '栏目编码',
    channel_name VARCHAR(100) NOT NULL COMMENT '栏目名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父栏目ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    UNIQUE KEY uk_cms_channel_site_code (site_code, channel_code)
) ENGINE=InnoDB COMMENT='CMS栏目表';

-- CMS审核记录表
CREATE TABLE IF NOT EXISTS cms_audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT NOT NULL COMMENT '内容ID',
    audit_level TINYINT NOT NULL COMMENT '审核级别: 1初审 2复审 3终审',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    auditor_name VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    result VARCHAR(30) NOT NULL COMMENT '审核结果: approved/rejected',
    opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    audit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    INDEX idx_cms_audit_content (content_id)
) ENGINE=InnoDB COMMENT='CMS审核记录表';

-- 信息公开审核记录表
CREATE TABLE IF NOT EXISTS disclosure_audit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no VARCHAR(50) NOT NULL COMMENT '申请编号',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    action VARCHAR(30) DEFAULT NULL COMMENT '操作: approve/reject/transfer',
    opinion VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_disclosure_audit_apply (apply_no)
) ENGINE=InnoDB COMMENT='信息公开审核记录表';

-- 留言分派记录表
CREATE TABLE IF NOT EXISTS message_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '留言ID',
    from_dept VARCHAR(50) DEFAULT NULL COMMENT '来源部门',
    to_dept VARCHAR(50) NOT NULL COMMENT '目标部门',
    assignee_id BIGINT DEFAULT NULL COMMENT '分派人ID',
    assign_reason VARCHAR(500) DEFAULT NULL COMMENT '分派原因',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分派时间',
    status VARCHAR(30) DEFAULT 'assigned' COMMENT '状态: assigned/accepted/rejected',
    INDEX idx_message_assignment_message (message_id)
) ENGINE=InnoDB COMMENT='留言分派记录表';

-- 留言督办表
CREATE TABLE IF NOT EXISTS message_supervision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '留言ID',
    deadline DATETIME NOT NULL COMMENT '督办期限',
    remind_count INT DEFAULT 0 COMMENT '提醒次数',
    overdue TINYINT DEFAULT 0 COMMENT '是否超期: 0否 1是',
    supervise_status VARCHAR(30) DEFAULT 'normal' COMMENT '督办状态: normal/warning/overdue',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message_supervision_message (message_id)
) ENGINE=InnoDB COMMENT='留言督办表';

-- 民意调查表
CREATE TABLE IF NOT EXISTS survey (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_code VARCHAR(50) NOT NULL COMMENT '调查编码',
    title VARCHAR(200) NOT NULL COMMENT '调查标题',
    description TEXT DEFAULT NULL COMMENT '调查说明',
    status VARCHAR(30) DEFAULT 'draft' COMMENT '状态: draft/published/closed',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_survey_code (survey_code)
) ENGINE=InnoDB COMMENT='民意调查表';

-- 民意调查题目表
CREATE TABLE IF NOT EXISTS survey_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL COMMENT '调查ID',
    question_type VARCHAR(30) NOT NULL COMMENT '题目类型: single/multiple/text/rating',
    title VARCHAR(300) NOT NULL COMMENT '题目内容',
    options_json JSON DEFAULT NULL COMMENT '选项(JSON)',
    required TINYINT DEFAULT 0 COMMENT '是否必答: 0否 1是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    INDEX idx_survey_question_survey (survey_id)
) ENGINE=InnoDB COMMENT='民意调查题目表';

-- 民意调查答卷表
CREATE TABLE IF NOT EXISTS survey_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL COMMENT '调查ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    answer_json JSON NOT NULL COMMENT '答案数据(JSON)',
    submit_ip VARCHAR(50) DEFAULT NULL COMMENT '提交IP',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    INDEX idx_survey_answer_survey (survey_id)
) ENGINE=InnoDB COMMENT='民意调查答卷表';
