-- =====================================================
-- 政府网站集约化平台 数据库初始化脚本 (单体模式)
-- MySQL 8.0+ / 9.x  - 单库 gov_platform
-- 覆盖全部25个业务模块 (A组-E组, 40+张表)
-- 设计规范: 所有业务表含 created_by/created_at/updated_by/updated_at/deleted/version 公共字段
-- =====================================================

CREATE DATABASE IF NOT EXISTS gov_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE gov_platform;

-- =====================================================
-- 0. 基础数据 & 字典
-- =====================================================

-- 政府部门表 (所有模块共用)
CREATE TABLE IF NOT EXISTS gov_dept (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_code       VARCHAR(50)  NOT NULL COMMENT '部门编码(如 EDU/HEA/FIN)',
    dept_name       VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id       BIGINT       DEFAULT 0 COMMENT '上级部门ID',
    level           TINYINT      DEFAULT 1 COMMENT '层级',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    site_code       VARCHAR(50)  COMMENT '关联子站编码(D1)',
    contact_person  VARCHAR(50)  COMMENT '联系人',
    contact_phone   VARCHAR(20)  COMMENT '联系电话',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
    version         INT          DEFAULT 0 COMMENT '乐观锁版本号',
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政府部门';


-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    dict_name       VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_code       VARCHAR(50)  NOT NULL COMMENT '字典编码',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    remark          VARCHAR(500) COMMENT '备注',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    dict_code       VARCHAR(50)  NOT NULL COMMENT '所属字典编码',
    label           VARCHAR(100) NOT NULL COMMENT '数据标签',
    value           VARCHAR(100) NOT NULL COMMENT '数据值',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    color           VARCHAR(20)  COMMENT '标签颜色(前端展示)',
    remark          VARCHAR(500) COMMENT '备注',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_key      VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value    TEXT         NOT NULL COMMENT '配置值',
    config_desc     VARCHAR(500) COMMENT '配置说明',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- =====================================================
-- A组: AI智能服务 (A1-A5)
-- =====================================================

-- A1 热搜词表
CREATE TABLE IF NOT EXISTS hot_search_word (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '热搜词ID',
    keyword         VARCHAR(100) NOT NULL COMMENT '搜索词',
    search_count    INT          DEFAULT 0 COMMENT '搜索次数',
    category        VARCHAR(50)  COMMENT '分类',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0隐藏 1显示',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜词(A1)';

-- A1 搜索历史表
CREATE TABLE IF NOT EXISTS search_history (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    keyword         VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    result_count    INT          DEFAULT 0 COMMENT '搜索结果数',
    search_type     VARCHAR(20)  DEFAULT 'all' COMMENT '搜索类型: all/content/service',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史(A1)';

-- A2 知识库文档
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    title           VARCHAR(300) NOT NULL COMMENT '文档标题',
    content         LONGTEXT     COMMENT '文档内容',
    summary         VARCHAR(500) COMMENT '摘要',
    source          VARCHAR(200) COMMENT '来源',
    dept_code       VARCHAR(50)  COMMENT '所属部门编码',
    category        VARCHAR(50)  COMMENT '分类: 政策文件/办事指南/常见问题',
    tags            VARCHAR(500) COMMENT '标签(逗号分隔)',
    file_url        VARCHAR(500) COMMENT '附件URL',
    status          TINYINT      DEFAULT 0 COMMENT '状态 0待索引 1已索引 2索引失败',
    vector_id       VARCHAR(100) COMMENT '向量ID(ES embedding)',
    publish_at      DATETIME     COMMENT '发布日期',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_dept (dept_code),
    INDEX idx_category (category),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政务知识库文档(A2)';

-- A3 AI问答会话表 (多轮对话)
CREATE TABLE IF NOT EXISTS chat_conversation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    conv_id         VARCHAR(64)  NOT NULL COMMENT '会话唯一标识(UUID)',
    user_id         BIGINT       COMMENT '用户ID(匿名用户为NULL)',
    title           VARCHAR(200) COMMENT '会话标题(自动生成或用户命名)',
    model_name      VARCHAR(50)  DEFAULT 'gpt-4' COMMENT '使用的AI模型',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0已删除 1正常',
    msg_count       INT          DEFAULT 0 COMMENT '消息数',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_conv_id (conv_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答会话(A3)';

-- A3 AI问答消息表 (每条对话的消息)
CREATE TABLE IF NOT EXISTS chat_message (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conv_id             VARCHAR(64)  NOT NULL COMMENT '所属会话ID',
    role                VARCHAR(20)  NOT NULL COMMENT '角色: user/assistant/system',
    content             LONGTEXT     NOT NULL COMMENT '消息内容',
    sources             TEXT         COMMENT '知识库来源(JSON数组: [{title,url,score}])',
    suggested_questions TEXT         COMMENT '推荐追问(JSON数组)',
    token_count         INT          DEFAULT 0 COMMENT 'Token数',
    elapsed_ms          INT          DEFAULT 0 COMMENT '响应耗时(毫秒)',
    rating              TINYINT      COMMENT '用户评价 1有用 0无用',
    created_at          DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间',
    INDEX idx_conv_id (conv_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答消息(A3)';

-- A4 敏感词库
CREATE TABLE IF NOT EXISTS sensitive_word (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '敏感词ID',
    word            VARCHAR(100) NOT NULL COMMENT '敏感词',
    category        VARCHAR(50)  COMMENT '分类: 政治/暴力/违法/广告/其他',
    level           TINYINT      DEFAULT 1 COMMENT '级别 1低 2中 3高',
    replace_with    VARCHAR(100) COMMENT '替换词(如 ***)',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_word (word),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库(A4)';

-- A4 敏感词过滤日志
CREATE TABLE IF NOT EXISTS sensitive_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    module          VARCHAR(50)  NOT NULL COMMENT '触发模块(A3问答/C1留言)',
    content         TEXT         NOT NULL COMMENT '原始内容',
    hit_words       VARCHAR(500) COMMENT '命中敏感词(逗号分隔)',
    level           TINYINT      COMMENT '最高敏感级别',
    action          VARCHAR(20)  DEFAULT 'block' COMMENT '处理动作: block/replace/audit',
    user_id         BIGINT       COMMENT '触发用户ID',
    source_ip       VARCHAR(50)  COMMENT '来源IP',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_module (module),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词过滤日志(A4)';

-- A5 用户反馈表 (AI回答评价)
CREATE TABLE IF NOT EXISTS chat_feedback (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '反馈ID',
    message_id      BIGINT       NOT NULL COMMENT '关联消息ID',
    conv_id         VARCHAR(64)  NOT NULL COMMENT '会话ID',
    user_id         BIGINT       COMMENT '用户ID',
    rating          TINYINT      NOT NULL COMMENT '评分 1满意 0不满意',
    feedback_type   VARCHAR(20)  COMMENT '反馈类型: irrelevant/inaccurate/offensive/other',
    content         VARCHAR(500) COMMENT '反馈内容',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv_id (conv_id),
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI问答反馈(A5)';


-- =====================================================
-- B组: 办事服务平台 (B1-B5)
-- =====================================================

-- B1 办事事项目录
CREATE TABLE IF NOT EXISTS service_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '事项ID',
    item_code       VARCHAR(50)  NOT NULL COMMENT '事项编码',
    item_name       VARCHAR(300) NOT NULL COMMENT '事项名称',
    category        VARCHAR(50)  COMMENT '分类: 个人/法人/综合',
    dept_code       VARCHAR(50)  COMMENT '承办部门编码',
    dept_name       VARCHAR(100) COMMENT '承办部门名称',
    description     TEXT         COMMENT '事项描述',
    legal_basis     TEXT         COMMENT '法律依据',
    time_limit      INT          DEFAULT 30 COMMENT '办理时限(工作日)',
    service_object  VARCHAR(200) COMMENT '服务对象',
    handling_method VARCHAR(200) COMMENT '办理方式: 窗口/网上/快递',
    process_desc    TEXT         COMMENT '办理流程描述',
    material_list   TEXT         COMMENT '申请材料列表(JSON)',
    form_schema     LONGTEXT     COMMENT '动态表单JSON Schema(B2)',
    form_id         BIGINT       COMMENT '关联表单定义ID(B2)',
    workflow_key    VARCHAR(100) COMMENT '流程定义Key(Flowable B3)',
    fee_description VARCHAR(500) COMMENT '收费说明',
    result_sample   VARCHAR(200) COMMENT '办理结果样例',
    online_flag     TINYINT      DEFAULT 1 COMMENT '是否可在线办理 0否 1是',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0下线 1上线',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_item_code (item_code),
    INDEX idx_dept (dept_code),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办事事项目录(B1)';

-- B2 动态表单定义表
CREATE TABLE IF NOT EXISTS form_definition (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '表单ID',
    form_code       VARCHAR(50)  NOT NULL COMMENT '表单编码',
    form_name       VARCHAR(200) NOT NULL COMMENT '表单名称',
    form_schema     LONGTEXT     NOT NULL COMMENT 'JSON Schema定义',
    form_ui         LONGTEXT     COMMENT 'UI布局配置(JSON)',
    form_version    INT          DEFAULT 1 COMMENT '版本号',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    remark          VARCHAR(500) COMMENT '备注',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_form_code (form_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态表单定义(B2)';

-- B3 办件申请记录表
CREATE TABLE IF NOT EXISTS service_application (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    application_no  VARCHAR(50)  NOT NULL COMMENT '申请编号(受理号)',
    item_id         BIGINT       NOT NULL COMMENT '事项ID',
    user_id         BIGINT       COMMENT '申请人用户ID',
    user_name       VARCHAR(100) COMMENT '申请人姓名',
    id_card         VARCHAR(20)  COMMENT '身份证号(AES加密)',
    phone           VARCHAR(20)  COMMENT '手机号',
    form_data       LONGTEXT     COMMENT '表单数据(JSON)',
    material_urls   TEXT         COMMENT '材料附件URL(JSON数组)',
    status          VARCHAR(30)  DEFAULT 'draft' COMMENT '状态: draft/submitted/初审/复审/approved/rejected/completed',
    current_node    VARCHAR(100) COMMENT '当前审批节点',
    workflow_inst_id VARCHAR(64) COMMENT 'Flowable流程实例ID',
    submit_at       DATETIME     COMMENT '提交时间',
    finish_at       DATETIME     COMMENT '办结时间',
    dept_code       VARCHAR(50)  COMMENT '办理部门',
    result_content  TEXT         COMMENT '办理结果说明',
    satisfaction    TINYINT      COMMENT '满意度评分 1-5',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_application_no (application_no),
    INDEX idx_item_id (item_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_dept (dept_code),
    INDEX idx_workflow (workflow_inst_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办件申请记录(B3)';

-- B3 审批记录表
CREATE TABLE IF NOT EXISTS service_approval_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批记录ID',
    application_id  BIGINT       NOT NULL COMMENT '申请ID',
    node_name       VARCHAR(100) NOT NULL COMMENT '审批节点名称',
    action          VARCHAR(20)  NOT NULL COMMENT '动作: approve/reject/return/transfer',
    opinion         TEXT         COMMENT '审批意见',
    operator_id     BIGINT       COMMENT '审批人ID',
    operator_name   VARCHAR(50)  COMMENT '审批人姓名',
    assignee_id     BIGINT       COMMENT '转办目标人ID',
    task_id         VARCHAR(64)  COMMENT 'Flowable任务ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_application (application_id),
    INDEX idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录(B3)';

-- B4 办事进度通知日志
CREATE TABLE IF NOT EXISTS service_notification_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知日志ID',
    application_id  BIGINT       NOT NULL COMMENT '申请ID',
    user_id         BIGINT       COMMENT '接收用户ID',
    channel         VARCHAR(20)  COMMENT '渠道: 站内/短信/微信',
    title           VARCHAR(200) COMMENT '通知标题',
    content         TEXT         COMMENT '通知内容',
    status          TINYINT      DEFAULT 0 COMMENT '发送状态 0未发送 1成功 2失败',
    error_msg       VARCHAR(500) COMMENT '失败原因',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_application (application_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办事通知日志(B4)';

-- B5 缴费记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '缴费ID',
    payment_no      VARCHAR(50)  NOT NULL COMMENT '缴费编号',
    application_id  BIGINT       COMMENT '关联申请ID',
    user_id         BIGINT       COMMENT '缴费人ID',
    amount          DECIMAL(12,2) NOT NULL COMMENT '金额(元)',
    fee_type        VARCHAR(50)  COMMENT '费用类型: 工本费/手续费/其他',
    payment_method  VARCHAR(30)  COMMENT '支付方式: wechat/alipay/bank/offline',
    status          VARCHAR(20)  DEFAULT 'unpaid' COMMENT '状态: unpaid/paid/refund',
    paid_at         DATETIME     COMMENT '支付时间',
    receipt_url     VARCHAR(500) COMMENT '电子票据URL',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_application (application_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费记录(B5)';

-- B5 电子证照表
CREATE TABLE IF NOT EXISTS e_certificate (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '证照ID',
    cert_no         VARCHAR(50)  NOT NULL COMMENT '证照编号',
    cert_type       VARCHAR(50)  NOT NULL COMMENT '证照类型: 营业执照/许可证/资质证书',
    cert_name       VARCHAR(200) NOT NULL COMMENT '证照名称',
    holder_name     VARCHAR(100) COMMENT '持证主体名称',
    holder_id       VARCHAR(20)  COMMENT '持证主体证件号',
    issuing_dept    VARCHAR(100) COMMENT '发证机关',
    issue_date      DATE         COMMENT '发证日期',
    expire_date     DATE         COMMENT '有效期至',
    file_url        VARCHAR(500) COMMENT '证照文件URL',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0无效 1有效',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_cert_no (cert_no),
    INDEX idx_holder (holder_id),
    INDEX idx_type (cert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子证照(B5)';


-- =====================================================
-- C组: 政民互动与公开 (C1-C5)
-- =====================================================

-- C1 政民互动留言表
CREATE TABLE IF NOT EXISTS message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '留言ID',
    message_no      VARCHAR(50)  NOT NULL COMMENT '留言编号',
    user_id         BIGINT       COMMENT '群众用户ID',
    user_name       VARCHAR(100) COMMENT '群众姓名',
    contact_phone   VARCHAR(20)  COMMENT '联系电话',
    title           VARCHAR(300) NOT NULL COMMENT '留言标题',
    content         TEXT         NOT NULL COMMENT '留言内容',
    category        VARCHAR(30)  DEFAULT 'consult' COMMENT '类型: consult/complaint/suggestion/report/other',
    is_public       TINYINT      DEFAULT 1 COMMENT '是否公开 0匿名 1公开',
    dept_code       VARCHAR(50)  COMMENT '目标部门(分派后)',
    assignee_id     BIGINT       COMMENT '处理人ID',
    status          VARCHAR(30)  DEFAULT 'pending' COMMENT '状态: pending/assigned/processing/replied/closed',
    tags            VARCHAR(200) COMMENT '标签(逗号分隔)',
    reply_content   TEXT         COMMENT '回复内容',
    reply_by        BIGINT       COMMENT '回复人ID',
    reply_at        DATETIME     COMMENT '回复时间',
    satisfaction    TINYINT      COMMENT '满意度评分 1-5',
    deadline        DATE         COMMENT '办理截止日期',
    finished_at     DATETIME     COMMENT '办结时间',
    source_ip       VARCHAR(50)  COMMENT '来源IP',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_message_no (message_no),
    INDEX idx_user (user_id),
    INDEX idx_dept (dept_code),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政民互动留言(C1)';

-- C1/C2 留言附件表
CREATE TABLE IF NOT EXISTS message_attachment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '附件ID',
    message_id      BIGINT       NOT NULL COMMENT '留言ID',
    file_name       VARCHAR(200) NOT NULL COMMENT '文件名',
    file_url        VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_size       BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    file_type       VARCHAR(50)  COMMENT '文件类型',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_message (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言附件(C1)';

-- C2 督办记录表
CREATE TABLE IF NOT EXISTS supervision_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '督办ID',
    business_type   VARCHAR(20)  NOT NULL COMMENT '业务类型: message/disclosure/service',
    business_id     BIGINT       NOT NULL COMMENT '业务记录ID',
    supervise_dept  VARCHAR(50)  COMMENT '督办部门',
    supervise_by    BIGINT       COMMENT '督办人ID',
    supervise_content TEXT       COMMENT '督办内容',
    deadline        DATE         COMMENT '要求办结日期',
    status          VARCHAR(20)  DEFAULT 'pending' COMMENT '状态: pending/resolved/ignored',
    result_content  TEXT         COMMENT '处理结果',
    resolved_at     DATETIME     COMMENT '解决时间',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_business (business_type, business_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='督办记录(C2)';

-- C3 依申请公开申请表
CREATE TABLE IF NOT EXISTS disclosure_apply (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申请ID',
    apply_no        VARCHAR(50)  NOT NULL COMMENT '申请编号',
    applicant_name  VARCHAR(100) NOT NULL COMMENT '申请人姓名',
    applicant_type  VARCHAR(20)  DEFAULT 'individual' COMMENT '申请人类型: individual/enterprise/other',
    id_card         VARCHAR(20)  COMMENT '身份证号(AES加密)',
    phone           VARCHAR(20)  COMMENT '联系电话',
    email           VARCHAR(100) COMMENT '电子邮箱',
    address         VARCHAR(300) COMMENT '通讯地址',
    content_desc    TEXT         NOT NULL COMMENT '所需信息内容描述',
    purpose         VARCHAR(500) COMMENT '用途说明',
    obtain_method   VARCHAR(30)  DEFAULT 'mail' COMMENT '获取方式: mail/email/online/self',
    dept_code       VARCHAR(50)  COMMENT '受理部门',
    status          VARCHAR(30)  DEFAULT 'received' COMMENT '状态: draft/received/reviewing/replied/rejected/appealed/closed',
    workflow_inst_id VARCHAR(64) COMMENT 'Flowable流程实例ID',
    deadline        DATE         COMMENT '答复期限(15工作日)',
    reply_title     VARCHAR(300) COMMENT '答复标题',
    reply_content   TEXT         COMMENT '答复内容',
    reply_file_url  VARCHAR(500) COMMENT '答复文件URL',
    reply_at        DATETIME     COMMENT '答复时间',
    reject_reason   TEXT         COMMENT '驳回原因',
    appeal_content  TEXT         COMMENT '申诉内容',
    appeal_result   TEXT         COMMENT '申诉结果',
    fee_amount      DECIMAL(10,2) COMMENT '费用(元)',
    user_id         BIGINT       COMMENT '用户ID(登录用户)',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_apply_no (apply_no),
    INDEX idx_user (user_id),
    INDEX idx_dept (dept_code),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='依申请公开记录(C3)';

-- C4 依申请公开审核记录表
CREATE TABLE IF NOT EXISTS disclosure_review_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核记录ID',
    apply_id        BIGINT       NOT NULL COMMENT '申请ID',
    node_name       VARCHAR(100) NOT NULL COMMENT '审核节点名称',
    action          VARCHAR(20)  NOT NULL COMMENT '动作: approve/reject/return',
    opinion         TEXT         COMMENT '审核意见',
    operator_id     BIGINT       COMMENT '审核人ID',
    operator_name   VARCHAR(50)  COMMENT '审核人姓名',
    task_id         VARCHAR(64)  COMMENT 'Flowable任务ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_apply (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='依申请公开审核记录(C4)';

-- C5 调查问卷表
CREATE TABLE IF NOT EXISTS survey (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '问卷ID',
    title           VARCHAR(300) NOT NULL COMMENT '问卷标题',
    description     TEXT         COMMENT '问卷说明',
    category        VARCHAR(30)  COMMENT '类型: satisfaction/opinion/evaluation/other',
    status          TINYINT      DEFAULT 0 COMMENT '状态 0草稿 1进行中 2已结束',
    start_at        DATETIME     COMMENT '开始时间',
    end_at          DATETIME     COMMENT '结束时间',
    dept_code       VARCHAR(50)  COMMENT '发布部门',
    target_count    INT          COMMENT '目标收集数',
    actual_count    INT          DEFAULT 0 COMMENT '实际收集数',
    is_anonymous    TINYINT      DEFAULT 1 COMMENT '是否匿名 0实名 1匿名',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_dept (dept_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调查问卷(C5)';

-- C5 问卷题目表
CREATE TABLE IF NOT EXISTS survey_question (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    survey_id       BIGINT       NOT NULL COMMENT '所属问卷ID',
    question_no     INT          NOT NULL COMMENT '题号',
    question_type   VARCHAR(20)  NOT NULL COMMENT '题型: single/multi/text/score',
    title           VARCHAR(500) NOT NULL COMMENT '题目内容',
    options         TEXT         COMMENT '选项(JSON数组: [{label,value}])',
    required        TINYINT      DEFAULT 1 COMMENT '是否必答 0否 1是',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_survey (survey_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷题目(C5)';

-- C5 问卷答案表
CREATE TABLE IF NOT EXISTS survey_answer (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答案ID',
    survey_id       BIGINT       NOT NULL COMMENT '问卷ID',
    question_id     BIGINT       NOT NULL COMMENT '题目ID',
    respondent_id   BIGINT       COMMENT '答题人ID(匿名时为NULL)',
    answer_content  TEXT         NOT NULL COMMENT '回答内容',
    submitted_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_survey (survey_id),
    INDEX idx_question (question_id),
    INDEX idx_respondent (respondent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷答案(C5)';


-- =====================================================
-- D组: 部门集约化与内容管理 (D1-D5)
-- =====================================================

-- D1 部门子站表
CREATE TABLE IF NOT EXISTS cms_site (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '子站ID',
    site_code       VARCHAR(50)  NOT NULL COMMENT '子站编码',
    site_name       VARCHAR(200) NOT NULL COMMENT '子站名称',
    dept_code       VARCHAR(50)  NOT NULL COMMENT '所属部门编码',
    domain          VARCHAR(200) COMMENT '子站域名',
    logo_url        VARCHAR(500) COMMENT 'LOGO URL',
    banner_url      VARCHAR(500) COMMENT '横幅图片URL',
    icp_record      VARCHAR(100) COMMENT 'ICP备案号',
    template        VARCHAR(50)  DEFAULT 'default' COMMENT '模板标识',
    description     VARCHAR(500) COMMENT '子站描述',
    contact_phone   VARCHAR(20)  COMMENT '联系电话',
    contact_email   VARCHAR(100) COMMENT '联系邮箱',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_site_code (site_code),
    UNIQUE KEY uk_dept (dept_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门子站(D1)';

-- D2 CMS栏目表
CREATE TABLE IF NOT EXISTS cms_column (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '栏目ID',
    site_code       VARCHAR(50)  NOT NULL COMMENT '所属子站编码',
    parent_id       BIGINT       DEFAULT 0 COMMENT '父栏目ID',
    column_name     VARCHAR(100) NOT NULL COMMENT '栏目名称',
    column_type     VARCHAR(30)  DEFAULT 'list' COMMENT '栏目类型: list/link/single',
    link_url        VARCHAR(500) COMMENT '外部链接(当column_type=link)',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    is_show         TINYINT      DEFAULT 1 COMMENT '是否显示 0隐藏 1显示',
    template        VARCHAR(100) COMMENT '栏目模板',
    seo_keywords    VARCHAR(200) COMMENT 'SEO关键词',
    seo_description VARCHAR(500) COMMENT 'SEO描述',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_site (site_code),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS栏目(D2)';

-- D2 CMS内容表
CREATE TABLE IF NOT EXISTS cms_content (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '内容ID',
    site_code       VARCHAR(50)  NOT NULL COMMENT '所属子站编码',
    column_id       BIGINT       COMMENT '所属栏目ID',
    title           VARCHAR(500) NOT NULL COMMENT '标题',
    subtitle        VARCHAR(300) COMMENT '副标题',
    content         LONGTEXT     COMMENT '正文(HTML)',
    source          VARCHAR(200) COMMENT '来源',
    author          VARCHAR(100) COMMENT '作者',
    content_type    VARCHAR(30)  DEFAULT 'article' COMMENT '类型: article/notice/policy/guide/news',
    tags            VARCHAR(500) COMMENT '标签(逗号分隔)',
    summary         VARCHAR(500) COMMENT '摘要',
    cover_url       VARCHAR(500) COMMENT '封面图URL',
    attachments     TEXT         COMMENT '附件列表(JSON)',
    is_top          TINYINT      DEFAULT 0 COMMENT '是否置顶 0否 1是',
    top_expire_at   DATETIME     COMMENT '置顶过期时间',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          VARCHAR(30)  DEFAULT 'draft' COMMENT '状态: draft/初审/终审/published/rejected/offline',
    publish_at      DATETIME     COMMENT '发布时间',
    offline_at      DATETIME     COMMENT '下线时间',
    view_count      INT          DEFAULT 0 COMMENT '浏览量',
    es_sync_status  TINYINT      DEFAULT 0 COMMENT 'ES同步状态 0未同步 1已同步',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_site (site_code),
    INDEX idx_column (column_id),
    INDEX idx_status (status),
    INDEX idx_publish (publish_at),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS内容(D2)';

-- D2 CMS附件表
CREATE TABLE IF NOT EXISTS cms_attachment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '附件ID',
    content_id      BIGINT       COMMENT '关联内容ID',
    file_name       VARCHAR(200) NOT NULL COMMENT '文件名',
    file_url        VARCHAR(500) NOT NULL COMMENT '文件URL(MinIO)',
    file_size       BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    file_type       VARCHAR(50)  COMMENT 'MIME类型',
    download_count  INT          DEFAULT 0 COMMENT '下载次数',
    created_by      BIGINT       COMMENT '上传人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CMS附件(D2)';

-- D3 内容审核记录表
CREATE TABLE IF NOT EXISTS content_audit_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核记录ID',
    content_id      BIGINT       NOT NULL COMMENT '内容ID',
    audit_node      VARCHAR(50)  NOT NULL COMMENT '审核节点: 初审/终审',
    action          VARCHAR(20)  NOT NULL COMMENT '动作: approve/reject/return',
    opinion         TEXT         COMMENT '审核意见',
    operator_id     BIGINT       COMMENT '审核人ID',
    operator_name   VARCHAR(50)  COMMENT '审核人姓名',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容审核记录(D3)';

-- D4 信息公开目录表
CREATE TABLE IF NOT EXISTS disclosure_catalog (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '目录ID',
    parent_id       BIGINT       DEFAULT 0 COMMENT '父目录ID',
    name            VARCHAR(200) NOT NULL COMMENT '目录名称',
    code            VARCHAR(50)  COMMENT '目录编码',
    level           TINYINT      DEFAULT 1 COMMENT '层级',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    dept_code       VARCHAR(50)  COMMENT '所属部门',
    description     VARCHAR(500) COMMENT '目录说明',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0隐藏 1显示',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_parent (parent_id),
    INDEX idx_dept (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息公开目录(D4)';

-- D5 搜索索引配置表
CREATE TABLE IF NOT EXISTS search_index_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    index_name      VARCHAR(100) NOT NULL COMMENT 'ES索引名',
    display_name    VARCHAR(100) COMMENT '展示名称',
    data_source     VARCHAR(50)  COMMENT '数据源: cms_content/knowledge_doc/service_item',
    sync_strategy   VARCHAR(20)  DEFAULT 'incremental' COMMENT '同步策略: full/incremental',
    last_sync_at    DATETIME     COMMENT '最后同步时间',
    doc_count       INT          DEFAULT 0 COMMENT '文档数',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_index_name (index_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索索引配置(D5)';


-- =====================================================
-- E组: 统计分析与公共服务 (E1-E5)
-- =====================================================

-- E1 数据采集配置表
CREATE TABLE IF NOT EXISTS data_collection_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_name     VARCHAR(100) NOT NULL COMMENT '采集配置名称',
    data_source     VARCHAR(50)  NOT NULL COMMENT '数据源: pageview/service/interaction/ai',
    collect_type    VARCHAR(30)  COMMENT '采集方式: auto/manual',
    cron_expr       VARCHAR(50)  COMMENT '定时表达式',
    retention_days  INT          DEFAULT 365 COMMENT '数据保留天数',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据采集配置(E1)';

-- E1 统计数据事实表
CREATE TABLE IF NOT EXISTS stats_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    stats_date      DATE         NOT NULL COMMENT '统计日期',
    stats_hour      TINYINT      COMMENT '统计小时(日粒度)',
    dept_code       VARCHAR(50)  COMMENT '部门编码',
    dimension       VARCHAR(50)  NOT NULL COMMENT '维度: pv/uv/msg/service/ai/disclosure',
    metric_name     VARCHAR(100) NOT NULL COMMENT '指标名',
    metric_value    DECIMAL(14,4) DEFAULT 0 COMMENT '指标值',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_date (stats_date),
    INDEX idx_dimension (dimension),
    INDEX idx_dept (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统计数据(E1)';

-- E2 报表配置表
CREATE TABLE IF NOT EXISTS report_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '报表ID',
    report_name     VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_type     VARCHAR(30)  COMMENT '类型: daily/monthly/quarterly/custom',
    dimension       VARCHAR(100) COMMENT '统计维度(JSON)',
    date_range      VARCHAR(50)  COMMENT '时间范围',
    chart_config    LONGTEXT     COMMENT '图表配置(JSON)',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表配置(E2)';

-- E2 大屏配置表
CREATE TABLE IF NOT EXISTS dashboard_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '大屏ID',
    dashboard_name  VARCHAR(200) NOT NULL COMMENT '大屏名称',
    layout_config   LONGTEXT     COMMENT '布局配置(JSON)',
    refresh_interval INT         DEFAULT 60 COMMENT '刷新间隔(秒)',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大屏配置(E2)';

-- E3 考核周期表
CREATE TABLE IF NOT EXISTS performance_period (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '周期ID',
    period_name     VARCHAR(100) NOT NULL COMMENT '周期名称',
    period_type     VARCHAR(20)  NOT NULL COMMENT '周期类型: month/quarter/year',
    period_code     VARCHAR(20)  NOT NULL COMMENT '周期编码(如 2026-Q3)',
    start_date      DATE         NOT NULL COMMENT '开始日期',
    end_date        DATE         NOT NULL COMMENT '结束日期',
    status          TINYINT      DEFAULT 0 COMMENT '状态 0未开始 1进行中 2已结束',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_period_code (period_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核周期(E3)';

-- E3 绩效指标配置表
CREATE TABLE IF NOT EXISTS performance_indicator (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '指标ID',
    indicator_code  VARCHAR(50)  NOT NULL COMMENT '指标编码',
    indicator_name  VARCHAR(100) NOT NULL COMMENT '指标名称',
    dimension       VARCHAR(30)  COMMENT '维度: efficiency/quality/response/disclosure/content',
    weight          DECIMAL(5,2) DEFAULT 0 COMMENT '默认权重(%)',
    scoring_method  VARCHAR(30)  COMMENT '计分方式: linear/step/formula',
    scoring_config  TEXT         COMMENT '评分规则配置(JSON)',
    target_value    DECIMAL(10,2) COMMENT '目标值',
    unit            VARCHAR(20)  COMMENT '单位',
    data_source     VARCHAR(100) COMMENT '数据来源SQL/接口',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_indicator_code (indicator_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效指标(E3)';

-- E3 绩效评分表
CREATE TABLE IF NOT EXISTS performance_score (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评分ID',
    period_id       BIGINT       NOT NULL COMMENT '考核周期ID',
    dept_code       VARCHAR(50)  NOT NULL COMMENT '部门编码',
    indicator_id    BIGINT       NOT NULL COMMENT '指标ID',
    weight          DECIMAL(5,2) COMMENT '实际权重(%)',
    target_value    DECIMAL(10,2) COMMENT '目标值',
    actual_value    DECIMAL(10,2) COMMENT '实际值',
    score           DECIMAL(5,2) COMMENT '得分',
    score_detail    TEXT         COMMENT '评分明细说明',
    data_source     TEXT         COMMENT '数据来源追溯',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_period_dept_indicator (period_id, dept_code, indicator_id),
    INDEX idx_period (period_id),
    INDEX idx_dept (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效评分(E3)';

-- E3 绩效申诉表
CREATE TABLE IF NOT EXISTS performance_appeal (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '申诉ID',
    score_id        BIGINT       NOT NULL COMMENT '评分记录ID',
    dept_code       VARCHAR(50)  NOT NULL COMMENT '申诉部门',
    appeal_content  TEXT         NOT NULL COMMENT '申诉内容',
    appeal_evidence TEXT         COMMENT '申诉证据(附件URL)',
    status          VARCHAR(20)  DEFAULT 'pending' COMMENT '状态: pending/accepted/rejected',
    review_opinion  TEXT         COMMENT '审核意见',
    review_by       BIGINT       COMMENT '审核人ID',
    reviewed_at     DATETIME     COMMENT '审核时间',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_score (score_id),
    INDEX idx_dept (dept_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='绩效申诉(E3)';


-- E4 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL COMMENT '用户名/工号',
    password        VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    real_name       VARCHAR(100) COMMENT '真实姓名',
    id_card         VARCHAR(20)  COMMENT '身份证号(AES)',
    phone           VARCHAR(20)  COMMENT '手机号',
    email           VARCHAR(100) COMMENT '邮箱',
    avatar_url      VARCHAR(500) COMMENT '头像URL',
    user_type       VARCHAR(20)  DEFAULT 'staff' COMMENT '类型: citizen/staff/enterprise/admin',
    dept_code       VARCHAR(50)  COMMENT '所属部门编码',
    position        VARCHAR(100) COMMENT '职务',
    last_login_at   DATETIME     COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50)  COMMENT '最后登录IP',
    login_count     INT          DEFAULT 0 COMMENT '登录次数',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用 2锁定',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_username (username),
    INDEX idx_dept (dept_code),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户(E4)';

-- E4 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_code       VARCHAR(50)  NOT NULL COMMENT '角色编码',
    role_name       VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type       VARCHAR(20)  DEFAULT 'admin' COMMENT '类型: admin/dept/normal',
    data_scope      VARCHAR(20)  DEFAULT 'dept' COMMENT '数据范围: all/dept/self',
    description     VARCHAR(500) COMMENT '角色描述',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色(E4)';

-- E4 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    role_id         BIGINT       NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联(E4)';

-- E4 系统权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '权限ID',
    parent_id       BIGINT       DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_key  VARCHAR(100) COMMENT '权限标识(如 sys:user:add)',
    menu_type       VARCHAR(20)  COMMENT '类型: menu/button/api',
    route_path      VARCHAR(200) COMMENT '前端路由',
    component       VARCHAR(200) COMMENT '前端组件',
    icon            VARCHAR(50)  COMMENT '图标',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    is_show         TINYINT      DEFAULT 1 COMMENT '是否显示',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_permission_key (permission_key),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限(E4)';

-- E4 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    role_id         BIGINT       NOT NULL COMMENT '角色ID',
    permission_id   BIGINT       NOT NULL COMMENT '权限ID',
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role (role_id),
    INDEX idx_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联(E4)';

-- E4 操作审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_id         BIGINT       COMMENT '操作人ID',
    username        VARCHAR(50)  COMMENT '操作人用户名',
    module          VARCHAR(50)  NOT NULL COMMENT '操作模块',
    operation       VARCHAR(50)  NOT NULL COMMENT '操作类型: create/update/delete/login',
    business_type   VARCHAR(50)  COMMENT '业务类型',
    business_id     BIGINT       COMMENT '业务记录ID',
    request_url     VARCHAR(500) COMMENT '请求URL',
    request_method  VARCHAR(10)  COMMENT '请求方法',
    request_params  TEXT         COMMENT '请求参数',
    response_data   TEXT         COMMENT '响应数据',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0失败 1成功',
    error_msg       TEXT         COMMENT '错误信息',
    ip_address      VARCHAR(50)  COMMENT '操作IP',
    user_agent      VARCHAR(500) COMMENT '浏览器UA',
    elapsed_ms      INT          DEFAULT 0 COMMENT '耗时(ms)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_module (module),
    INDEX idx_operation (operation),
    INDEX idx_business (business_type, business_id),
    INDEX idx_created_at (created_at),
    INDEX idx_ip (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志(E4)';

-- E5 系统消息表
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    user_id         BIGINT       COMMENT '接收人ID(NULL表示全员)',
    msg_type        VARCHAR(30)  DEFAULT 'system' COMMENT '类型: system/notice/progress/warning/interact',
    title           VARCHAR(300) NOT NULL COMMENT '消息标题',
    content         TEXT         COMMENT '消息内容',
    business_type   VARCHAR(50)  COMMENT '关联业务类型',
    business_id     BIGINT       COMMENT '关联业务ID',
    is_read         TINYINT      DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    read_at         DATETIME     COMMENT '阅读时间',
    is_pushed       TINYINT      DEFAULT 0 COMMENT '是否已推送 0否 1是',
    push_channel    VARCHAR(30)  COMMENT '推送渠道: sms/wechat/email',
    push_result     TEXT         COMMENT '推送结果',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_type (msg_type),
    INDEX idx_read (is_read, user_id),
    INDEX idx_business (business_type, business_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统消息(E5)';

-- E5 消息模板表
CREATE TABLE IF NOT EXISTS message_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_code   VARCHAR(50)  NOT NULL COMMENT '模板编码',
    template_name   VARCHAR(200) NOT NULL COMMENT '模板名称',
    channel         VARCHAR(20)  NOT NULL COMMENT '渠道: site/sms/wechat/email',
    title_template  VARCHAR(300) COMMENT '标题模板(含占位符)',
    content_template TEXT        NOT NULL COMMENT '内容模板',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1启用',
    created_by      BIGINT       COMMENT '创建人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    UNIQUE KEY uk_template_code_channel (template_code, channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板(E5)';

-- =====================================================
-- 文件存储记录表 (MinIO文件索引)
-- =====================================================
CREATE TABLE IF NOT EXISTS file_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    file_name       VARCHAR(200) NOT NULL COMMENT '原始文件名',
    stored_name     VARCHAR(200) NOT NULL COMMENT '存储文件名',
    file_url        VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    bucket_name     VARCHAR(50)  NOT NULL COMMENT 'MinIO Bucket',
    file_size       BIGINT       NOT NULL COMMENT '文件大小(字节)',
    mime_type       VARCHAR(100) COMMENT 'MIME类型',
    md5_hash        VARCHAR(64)  COMMENT '文件MD5',
    module          VARCHAR(50)  COMMENT '所属模块',
    business_type   VARCHAR(50)  COMMENT '业务类型',
    business_id     BIGINT       COMMENT '业务ID',
    created_by      BIGINT       COMMENT '上传人ID',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT       COMMENT '更新人ID',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      DEFAULT 0,
    version         INT          DEFAULT 0,
    INDEX idx_module (module),
    INDEX idx_business (business_type, business_id),
    INDEX idx_md5 (md5_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件存储记录';


-- =====================================================
-- 初始化基础数据
-- =====================================================

-- 1. 政府部门基础数据
INSERT INTO gov_dept (dept_code, dept_name, parent_id, level, sort_order, site_code) VALUES
  ('ROOT',   '平台管理方',   0, 1, 0, NULL),
  ('ADMIN',  '系统管理部',   1, 2, 1, NULL),
  ('EDU',    '教育部',      1, 2, 2, 'edu'),
  ('HEA',    '卫生健康委',   1, 2, 3, 'hea'),
  ('FIN',    '财政部',      1, 2, 4, 'fin'),
  ('SOC',    '社会保障部',   1, 2, 5, 'soc'),
  ('HOU',    '住房保障部',   1, 2, 6, 'hou'),
  ('TAX',    '税务总局',    1, 2, 7, 'tax');

-- 2. 系统用户 (密码占位,需后续BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, user_type, dept_code) VALUES
  ('admin',    'placeholder_admin',    '系统管理员', '13800000001', 'admin', 'ADMIN'),
  ('zhang',    'placeholder_zhang',    '张三',       '13800000011', 'staff', 'EDU'),
  ('li',       'placeholder_li',       '李四',       '13800000012', 'staff', 'SOC'),
  ('wang',     'placeholder_wang',     '王五',       '13800000013', 'staff', 'FIN'),
  ('zhao',     'placeholder_zhao',     '赵六',       '13800000014', 'staff', 'HEA'),
  ('sun',      'placeholder_sun',      '孙七',       '13800000015', 'staff', 'HOU'),
  ('citizen1', 'placeholder_citizen1', '普通群众A',  '13900000001', 'citizen', NULL),
  ('citizen2', 'placeholder_citizen2', '普通群众B',  '13900000002', 'citizen', NULL);

-- 3. 系统角色
INSERT INTO sys_role (role_code, role_name, role_type, data_scope, description) VALUES
  ('SUPER_ADMIN',  '超级管理员',   'admin', 'all',  '系统超级管理员，拥有全部权限'),
  ('DEPT_ADMIN',   '部门管理员',   'dept',  'dept', '部门级管理员，管理本部门内容与用户'),
  ('CONTENT_MGR',  '内容管理员',   'normal','dept', '内容发布与维护'),
  ('APPROVER',     '审批员',       'normal','dept', '办事事项审批'),
  ('INTERACTION',  '互动管理员',   'normal','dept', '政民互动留言处理'),
  ('DISCLOSURE',   '公开审核员',   'normal','dept', '依申请公开审核'),
  ('STATS_VIEWER', '统计查看员',   'normal','all',  '查看统计报表与绩效'),
  ('OPERATOR',     '普通操作员',   'normal','self', '基本操作权限');

-- 4. 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
  (1, 1),   -- admin 超级管理员
  (2, 2), (2, 3), (2, 5),  -- zhang 部门管理员+内容管理员+互动管理员
  (3, 2), (3, 4),           -- li   部门管理员+审批员
  (4, 2), (4, 6),           -- wang 部门管理员+公开审核员
  (5, 2), (5, 7),           -- zhao 部门管理员+统计查看员
  (6, 2), (6, 3);           -- sun  部门管理员+内容管理员

-- 5. 字典类型
INSERT INTO sys_dict_type (dict_code, dict_name, remark) VALUES
  ('message_category',      '留言分类',     '政民互动留言类型'),
  ('service_item_category', '办事事项分类', '办事服务事项分类'),
  ('content_type',          '内容类型',     'CMS内容类型'),
  ('disclosure_status',     '公开状态',     '依申请公开状态'),
  ('site_template',         '站点模板',     '部门子站模板'),
  ('sensitive_category',    '敏感词分类',   '敏感词库分类');

-- 6. 字典数据
INSERT INTO sys_dict_data (dict_code, label, value, sort_order, color) VALUES
  ('message_category', '咨询',     'consult',   1, 'blue'),
  ('message_category', '投诉',     'complaint', 2, 'red'),
  ('message_category', '建议',     'suggestion',3, 'orange'),
  ('message_category', '举报',     'report',    4, 'red'),
  ('message_category', '其他',     'other',     5, 'gray'),
  ('content_type',     '文章',     'article',   1, NULL),
  ('content_type',     '通知公告',  'notice',    2, NULL),
  ('content_type',     '政策文件',  'policy',    3, NULL),
  ('content_type',     '办事指南',  'guide',     4, NULL),
  ('content_type',     '新闻动态',  'news',      5, NULL),
  ('sensitive_category', '政治',  'politics',   1, 'red'),
  ('sensitive_category', '暴力',  'violence',   2, 'red'),
  ('sensitive_category', '违法',  'illegal',    3, 'red'),
  ('sensitive_category', '广告',  'advertise',  4, 'orange'),
  ('sensitive_category', '其他',  'other',      5, 'gray');

-- 7. 部门子站初始数据
INSERT INTO cms_site (site_code, site_name, dept_code, template, status) VALUES
  ('edu', '教育部门户',    'EDU', 'edu-template', 1),
  ('hea', '卫生健康委门户','HEA', 'default', 1),
  ('fin', '财政部门户',    'FIN', 'default', 1),
  ('soc', '社会保障部门户','SOC', 'default', 1),
  ('hou', '住房保障部门户','HOU', 'default', 1),
  ('tax', '税务总局门户', 'TAX', 'default', 1);

-- 8. 栏目初始数据 (教育部示例)
INSERT INTO cms_column (site_code, parent_id, column_name, column_type, sort_order) VALUES
  ('edu', 0, '首页',        'list', 1),
  ('edu', 0, '机构概况',    'list', 2),
  ('edu', 0, '通知公告',    'list', 3),
  ('edu', 0, '政策文件',    'list', 4),
  ('edu', 0, '办事服务',    'list', 5),
  ('edu', 0, '互动交流',    'list', 6),
  ('edu', 0, '信息公开',    'list', 7),
  ('edu', 3, '教育部通知',  'list', 1),
  ('edu', 3, '公示公告',    'list', 2),
  ('edu', 4, '法律法规',    'list', 1),
  ('edu', 4, '政策解读',    'list', 2);

-- 9. 信息公开目录初始数据
INSERT INTO disclosure_catalog (parent_id, name, code, level, sort_order) VALUES
  (0, '机构职能',     'ORG',     1, 1),
  (0, '法规文件',     'LAW',     1, 2),
  (0, '规划计划',     'PLAN',    1, 3),
  (0, '统计数据',     'STATS',   1, 4),
  (0, '财政信息',     'FINANCE', 1, 5),
  (0, '人事信息',     'HR',      1, 6),
  (1, '机构设置',     'ORG_SET', 2, 1),
  (1, '领导信息',     'ORG_LEAD',2, 2),
  (1, '联系方式',     'ORG_CONT',2, 3);


-- 10. 办事事项初始数据
INSERT INTO service_item (item_code, item_name, category, dept_code, dept_name, time_limit, description, online_flag, status) VALUES
  ('EDU-001', '高校毕业生就业补贴申请',         '个人', 'EDU', '教育部',      30, '为高校毕业生提供就业补贴申领服务', 1, 1),
  ('EDU-002', '教师资格认定申请',               '个人', 'EDU', '教育部',      45, '教师资格认定在线申请', 1, 1),
  ('SOC-001', '社会保障卡申领',                 '个人', 'SOC', '社会保障部',   15, '社会保障卡在线申领服务', 1, 1),
  ('SOC-002', '养老保险待遇申领',               '个人', 'SOC', '社会保障部',   30, '养老保险待遇申领', 1, 1),
  ('HOU-001', '公租房租赁补贴申请',             '个人', 'HOU', '住房保障部',   45, '公租房租赁补贴在线申请', 1, 1),
  ('FIN-001', '个人所得税年度汇算',             '个人', 'TAX', '税务总局',    60, '个人所得税年度汇算清缴', 1, 1),
  ('EDU-003', '民办学校办学许可证核发',         '法人', 'EDU', '教育部',      60, '民办学校办学许可证核发', 1, 1),
  ('HEA-001', '医疗机构执业许可证变更',         '法人', 'HEA', '卫生健康委',   30, '医疗机构执业许可证变更登记', 1, 1);

-- 11. 考核周期数据
INSERT INTO performance_period (period_name, period_type, period_code, start_date, end_date, status) VALUES
  ('2026年7月',       'month',   '2026-07', '2026-07-01', '2026-07-31', 1),
  ('2026年8月',       'month',   '2026-08', '2026-08-01', '2026-08-31', 0),
  ('2026年第三季度',  'quarter', '2026-Q3', '2026-07-01', '2026-09-30', 1),
  ('2026年第四季度',  'quarter', '2026-Q4', '2026-10-01', '2026-12-31', 0);

-- 12. 绩效指标数据
INSERT INTO performance_indicator (indicator_code, indicator_name, dimension, weight, scoring_method, target_value, unit) VALUES
  ('COMPLETE_RATE',   '办结率',      'efficiency', 20.00, 'linear',  95.00, '%'),
  ('AVG_DURATION',    '平均办理时长', 'efficiency', 15.00, 'linear',  20.00, '天'),
  ('OVERTIME_RATE',   '超时率',      'efficiency', 10.00, 'linear',  2.00,  '%'),
  ('SATISFACTION',    '群众满意度',   'quality',    20.00, 'linear',  4.50,  '分'),
  ('PASS_RATE',       '一次性通过率', 'quality',    10.00, 'linear',  85.00, '%'),
  ('REPLY_RATE',      '留言回复率',   'response',   10.00, 'linear',  95.00, '%'),
  ('REPLY_SPEED',     '留言回复时效', 'response',    5.00, 'linear',  3.00,  '天'),
  ('INTERACT_SAT',    '留言满意度',   'response',    5.00, 'linear',  4.00,  '分'),
  ('DISCLOSURE_RATE', '答复率',      'disclosure',  3.00, 'linear',  100.00, '%'),
  ('DISCLOSURE_TIME', '答复时效',    'disclosure',  2.00, 'linear',  15.00, '天'),
  ('CONTENT_COUNT',   '内容发布量',  'content',     0.00, 'linear',  50.00, '篇'),
  ('UPDATE_TIMELY',   '更新及时性',  'content',     0.00, 'linear',  90.00, '%');

-- 13. 绩效评分示例数据 (2026-Q3)
INSERT INTO performance_score (period_id, dept_code, indicator_id, weight, target_value, actual_value, score) VALUES
  (3, 'EDU', 1,  20.00, 95.00, 96.80, 96.80),
  (3, 'EDU', 2,  15.00, 20.00, 18.00, 15.00),
  (3, 'EDU', 3,  10.00, 2.00,  1.50,  10.00),
  (3, 'EDU', 4,  20.00, 4.50,  4.70,  20.89),
  (3, 'EDU', 6,  10.00, 95.00, 98.00, 10.00),
  (3, 'HEA', 1,  20.00, 95.00, 91.00, 91.00),
  (3, 'HEA', 2,  15.00, 20.00, 22.00, 13.50),
  (3, 'HEA', 4,  20.00, 4.50,  4.30,  19.11),
  (3, 'FIN', 1,  20.00, 95.00, 88.00, 88.00),
  (3, 'FIN', 4,  20.00, 4.50,  4.30,  19.11);

-- 14. 热搜词初始数据
INSERT INTO hot_search_word (keyword, search_count, category, sort_order) VALUES
  ('社保卡申领',    1256, '办事', 1),
  ('就业补贴',      890,  '政策', 2),
  ('公租房申请',    756,  '办事', 3),
  ('教师资格认定',  623,  '办事', 4),
  ('个人所得税',    512,  '税务', 5),
  ('养老保险',      478,  '社保', 6),
  ('高校毕业生',    389,  '教育', 7),
  ('医疗保险',      345,  '社保', 8);

-- 15. CMS内容示例数据 (教育部)
INSERT INTO cms_content (site_code, column_id, title, content, content_type, author, status, publish_at, created_by) VALUES
  ('edu', 3, '关于2026年教师资格认定工作的通知',
   CONCAT('<p>', '根据《教师资格条例》和教育部工作安排，现就2026年教师资格认定工作有关事项通知如下', '...</p>'),
   'notice', '教育部教师工作司', 'published', '2026-07-10 09:00:00', 2),
  ('edu', 4, '高校毕业生就业创业扶持政策解读',
   CONCAT('<p>', '为促进高校毕业生就业创业，教育部会同相关部门出台了一系列扶持政策', '...</p>'),
   'policy', '教育部高校学生司', 'published', '2026-07-08 10:30:00', 2),
  ('edu', 3, '2026年秋季学期开学工作部署',
   CONCAT('<p>', '各地各校要科学谋划2026年秋季学期开学工作，确保师生安全和教育教学秩序', '...</p>'),
   'notice', '教育部办公厅',     'draft', NULL, 2),
  ('edu', 5, '高校毕业生就业补贴申领指南',
   CONCAT('<p>', '高校毕业生就业补贴申领流程：1.登录平台 2.填写信息 3.提交审核', '...</p>'),
   'guide', '教育部学生司',     'published', '2026-07-01 14:00:00', 2);

-- 16. 知识库示例数据
INSERT INTO knowledge_doc (title, content, summary, dept_code, category, tags, status) VALUES
  ('社保卡申领指南',              '社会保障卡申领的具体流程和所需材料', '社保卡申领全流程指引',            'SOC', '办事指南',  '社保卡,社保', 1),
  ('个人所得税专项附加扣除政策',  '个人所得税专项附加扣除包括子女教育、继续教育、大病医疗', '个税专项附加扣除详解', 'TAX', '政策文件',  '税务,个税,扣除', 1),
  ('高校毕业生就业补贴政策问答',  'Q:哪些高校毕业生可以申请就业补贴 A:毕业两年内', '就业补贴常见问题汇总',       'EDU', '常见问题',  '就业,补贴,毕业生', 1);

-- 17. 敏感词示例数据
INSERT INTO sensitive_word (word, category, level, replace_with, status) VALUES
  ('测试敏感词1', 'politics', 3, '***', 1),
  ('测试敏感词2', 'other',    1, '***', 1);

-- 18. 系统配置初始数据
INSERT INTO sys_config (config_key, config_value, config_desc) VALUES
  ('ai.model.name',           'gpt-4',              'AI问答使用的模型'),
  ('ai.max_turns',            '10',                 'AI多轮对话最大轮数'),
  ('ai.timeout_seconds',      '30',                 'AI回答超时秒数'),
  ('ai.rate_limit_per_user',  '10',                 '每用户每分钟AI问答次数限制'),
  ('disclosure.deadline_days','15',                 '依申请公开法定答复期限(工作日)'),
  ('message.deadline_days',   '5',                  '留言办理期限(工作日)'),
  ('message.auto_assign',     'true',               '留言是否自动分派'),
  ('search.suggest_count',    '10',                 '搜索联想推荐数量'),
  ('search.hot_words_count',  '20',                 '热搜词显示数量'),
  ('site.default_template',   'default',            '默认站点模板'),
  ('performance.auto_score',  'true',               '是否自动计算绩效评分');

-- 19. 搜索索引配置初始数据
INSERT INTO search_index_config (index_name, display_name, data_source, sync_strategy) VALUES
  ('gov_content',   '内容索引',   'cms_content',   'incremental'),
  ('gov_knowledge', '知识库索引', 'knowledge_doc', 'incremental'),
  ('gov_service',   '办事索引',   'service_item',  'incremental');

-- 20. 消息模板初始数据
INSERT INTO message_template (template_code, template_name, channel, title_template, content_template) VALUES
  ('service_progress', '办事进度通知', 'site', '您的申请已更新', '您申请的`${itemName}(受理号:${applicationNo})已进入${nodeName}阶段。'),
  ('service_progress', '办事进度通知', 'sms',  '【政务平台】您的${itemName}申请已更新为${nodeName}，详情请登录平台查看。'),
  ('message_reply',    '留言回复通知', 'site', '您的留言已收到回复', '您于${createAt}提交的留言"${title}"已收到回复，请点击查看。'),
  ('disclosure_reply', '依申请公开答复通知', 'site', '您的依申请公开已答复', '您申请的"${contentDesc}"已答复，请登录平台查看答复内容。'),
  ('overtime_warning', '超时预警通知', 'site', '【超时预警】${businessType}即将超时', '编号${businessNo}的${businessType}距截止日期不足${daysLeft}天，请尽快处理。');


