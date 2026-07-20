-- =====================================================
-- 政民互动与依申请公开模块 - 数据库表结构整合脚本
-- 整合时间: 2026-07-20
-- 说明: 包含政民互动(留言/咨询/建议/投诉/意见征集/问卷)
--       和依申请公开(申请/保密文件/文件访问/审核记录)全部表结构
-- =====================================================

USE gov_platform;

-- =====================================================
-- 一、政民互动 - 留言系统
-- =====================================================

-- 1.1 留言表（基础表 + 增强字段）
CREATE TABLE IF NOT EXISTS message (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT       COMMENT '群众ID',
    user_name     VARCHAR(50)  COMMENT '群众姓名',
    title         VARCHAR(200) NOT NULL COMMENT '标题',
    type          VARCHAR(20)  DEFAULT '咨询' COMMENT '留言类型: 咨询/投诉/建议/求助',
    content       TEXT         NOT NULL COMMENT '内容',
    dept_code     VARCHAR(50)  COMMENT '分派部门',
    status        VARCHAR(20)  DEFAULT '待分派' COMMENT '状态: 待分派/已分派/已回复/已办结',
    reply_content TEXT         COMMENT '回复内容',
    reply_by      VARCHAR(50)  COMMENT '回复人',
    reply_time    DATETIME     DEFAULT NULL,
    rating        TINYINT      DEFAULT NULL COMMENT '群众评价: 1-5星',
    rating_content TEXT        DEFAULT NULL COMMENT '评价内容',
    is_public     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否公开: 1-公开, 0-私密',
    consult_no    VARCHAR(32)  DEFAULT NULL COMMENT '信件单号',
    supervise     TINYINT      DEFAULT 0 COMMENT '是否督办: 0否 1是',
    supervise_time DATETIME    DEFAULT NULL COMMENT '督办时间',
    deadline      DATETIME     DEFAULT NULL COMMENT '处理期限',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT      DEFAULT 0,
    UNIQUE INDEX idx_consult_no (consult_no)
) ENGINE=InnoDB COMMENT='政民互动留言';

-- 1.2 留言回复记录表（支持多轮对话）
CREATE TABLE IF NOT EXISTS message_reply (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id  BIGINT NOT NULL COMMENT '留言ID',
    user_id     BIGINT DEFAULT NULL COMMENT '回复用户ID（C端用户）',
    user_name   VARCHAR(50) NOT NULL COMMENT '回复人姓名',
    user_type   VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '回复人类型: ADMIN-管理员, USER-群众',
    content     TEXT NOT NULL COMMENT '回复内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '回复时间',
    INDEX idx_message_id (message_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='留言回复记录（多轮对话）';

-- 1.3 留言分派记录表
CREATE TABLE IF NOT EXISTS message_assignment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id  BIGINT NOT NULL COMMENT '留言ID',
    from_dept   VARCHAR(50) DEFAULT NULL COMMENT '来源部门',
    to_dept     VARCHAR(50) NOT NULL COMMENT '目标部门',
    assignee_id BIGINT DEFAULT NULL COMMENT '分派人ID',
    assign_reason VARCHAR(500) DEFAULT NULL COMMENT '分派原因',
    assign_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '分派时间',
    status      VARCHAR(30) DEFAULT 'assigned' COMMENT '状态: assigned/accepted/rejected',
    INDEX idx_message_assignment_message (message_id)
) ENGINE=InnoDB COMMENT='留言分派记录表';

-- 1.4 留言督办表
CREATE TABLE IF NOT EXISTS message_supervision (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id  BIGINT NOT NULL COMMENT '留言ID',
    deadline    DATETIME NOT NULL COMMENT '督办期限',
    supervisor  VARCHAR(50) DEFAULT NULL COMMENT '督办人',
    content     TEXT DEFAULT NULL COMMENT '督办内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_message_supervision_message (message_id)
) ENGINE=InnoDB COMMENT='留言督办表';

-- =====================================================
-- 二、政民互动 - 咨询系统
-- =====================================================

-- 2.1 咨询表
CREATE TABLE IF NOT EXISTS consultation (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id       BIGINT DEFAULT NULL COMMENT '提交用户ID',
    consult_no    VARCHAR(30) NOT NULL UNIQUE COMMENT '咨询单号',
    real_name     VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone         VARCHAR(20) NOT NULL COMMENT '手机号码',
    telephone     VARCHAR(20) DEFAULT NULL COMMENT '电话号码',
    email         VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    title         VARCHAR(100) NOT NULL COMMENT '咨询主题',
    city          VARCHAR(50) NOT NULL COMMENT '咨询市州',
    district      VARCHAR(50) DEFAULT NULL COMMENT '事件发生地-区',
    street        VARCHAR(50) DEFAULT NULL COMMENT '事件发生地-街道',
    address       VARCHAR(200) DEFAULT NULL COMMENT '事件发生具体地址',
    attachment    LONGTEXT DEFAULT NULL COMMENT '附件(JSON格式,含base64图片数据)',
    content       TEXT NOT NULL COMMENT '咨询内容',
    is_public     TINYINT(1) DEFAULT 1 COMMENT '是否愿意公开: 1是 0否',
    is_confidential TINYINT(1) DEFAULT 0 COMMENT '是否保密: 1是 0否',
    status        VARCHAR(20) DEFAULT '待受理' COMMENT '状态: 待受理/处理中/已答复/已办结',
    reply_content TEXT DEFAULT NULL COMMENT '答复内容',
    reply_by      VARCHAR(50) DEFAULT NULL COMMENT '答复人',
    reply_time    DATETIME DEFAULT NULL COMMENT '答复时间',
    deadline      DATE DEFAULT NULL COMMENT '答复期限',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT DEFAULT 0 COMMENT '创建人',
    update_by     BIGINT DEFAULT 0 COMMENT '更新人',
    deleted       INT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    INDEX idx_consult_no (consult_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_city (city),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政民互动-咨询表';

-- =====================================================
-- 三、政民互动 - 建议系统
-- =====================================================

-- 3.1 建议表
CREATE TABLE IF NOT EXISTS suggestion (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT DEFAULT NULL COMMENT '提交用户ID',
    suggest_no    VARCHAR(32) NOT NULL COMMENT '建议单号',
    title         VARCHAR(100) NOT NULL COMMENT '建议标题',
    type          VARCHAR(20) DEFAULT '网站建议' COMMENT '建议类型: 网站建议/部门建议/我要纠错/助企惠企服务专区建议',
    real_name     VARCHAR(50) NOT NULL COMMENT '建议人姓名',
    id_card       VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    email         VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    phone         VARCHAR(20) NOT NULL COMMENT '联系电话',
    address       VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    province      VARCHAR(50) DEFAULT NULL COMMENT '省份',
    city          VARCHAR(50) DEFAULT NULL COMMENT '城市',
    district      VARCHAR(50) DEFAULT NULL COMMENT '区县',
    detail_address VARCHAR(100) DEFAULT NULL COMMENT '具体地址',
    content       TEXT NOT NULL COMMENT '建议内容',
    is_public     TINYINT(1) DEFAULT 1 COMMENT '是否公开: 1-是, 0-否',
    is_secret     TINYINT(1) DEFAULT 0 COMMENT '是否保密: 1-是, 0-否',
    status        VARCHAR(20) DEFAULT '待受理' COMMENT '状态: 待受理/处理中/已答复/已办结',
    reply_content TEXT DEFAULT NULL COMMENT '答复内容',
    reply_by      VARCHAR(50) DEFAULT NULL COMMENT '答复人',
    reply_time    DATETIME DEFAULT NULL COMMENT '答复时间',
    deadline      DATE DEFAULT NULL COMMENT '答复期限',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by     BIGINT DEFAULT NULL COMMENT '创建人',
    update_by     BIGINT DEFAULT NULL COMMENT '更新人',
    deleted       INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    UNIQUE INDEX idx_suggest_no (suggest_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政民互动-建议表';

-- =====================================================
-- 四、政民互动 - 投诉系统
-- =====================================================

-- 4.1 投诉表
CREATE TABLE IF NOT EXISTS complaint (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT DEFAULT NULL COMMENT '提交用户ID',
    complaint_no    VARCHAR(32) NOT NULL COMMENT '投诉单号',
    title           VARCHAR(100) NOT NULL COMMENT '投诉标题',
    -- 投诉单位（三级层级）
    complaint_unit_l1 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-一级(市州)',
    complaint_unit_l2 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-二级(部门)',
    complaint_unit_l3 VARCHAR(50) DEFAULT NULL COMMENT '投诉单位-三级(子部门)',
    -- 问题类型（三级层级）
    problem_type_l1 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-一级',
    problem_type_l2 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-二级',
    problem_type_l3 VARCHAR(50) DEFAULT NULL COMMENT '问题类型-三级',
    -- 投诉人信息
    real_name       VARCHAR(50) NOT NULL COMMENT '投诉人姓名',
    id_card         VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    email           VARCHAR(100) DEFAULT NULL COMMENT '电子邮箱',
    phone           VARCHAR(20) NOT NULL COMMENT '联系电话',
    address         VARCHAR(200) DEFAULT NULL COMMENT '联系地址',
    -- 事件发生地
    province        VARCHAR(50) DEFAULT NULL COMMENT '省份',
    city            VARCHAR(50) DEFAULT NULL COMMENT '城市',
    district        VARCHAR(50) DEFAULT NULL COMMENT '区县',
    detail_address  VARCHAR(100) DEFAULT NULL COMMENT '具体地址',
    -- 内容
    content         TEXT NOT NULL COMMENT '投诉内容',
    attachment      TEXT DEFAULT NULL COMMENT '附件(JSON)',
    is_public       TINYINT(1) DEFAULT 1 COMMENT '是否公开: 1-是, 0-否',
    is_secret       TINYINT(1) DEFAULT 0 COMMENT '是否保密: 1-是, 0-否',
    -- 状态流转
    status          VARCHAR(20) DEFAULT '待受理' COMMENT '状态: 待受理/处理中/已答复/已办结',
    reply_content   TEXT DEFAULT NULL COMMENT '答复内容',
    reply_by        VARCHAR(50) DEFAULT NULL COMMENT '答复人',
    reply_time      DATETIME DEFAULT NULL COMMENT '答复时间',
    deadline        DATE DEFAULT NULL COMMENT '答复期限',
    -- BaseEntity字段
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    UNIQUE INDEX idx_complaint_no (complaint_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政民互动-投诉表';

-- =====================================================
-- 五、政民互动 - 意见征集系统
-- =====================================================

-- 5.1 意见征集表
CREATE TABLE IF NOT EXISTS collection (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL COMMENT '征集主题',
    description     TEXT COMMENT '征集说明',
    dept_name       VARCHAR(100) DEFAULT NULL COMMENT '征集单位',
    contact_name    VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone   VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    attachment_name VARCHAR(200) DEFAULT NULL COMMENT '附件名称',
    attachment_url  VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    start_date      DATE NOT NULL COMMENT '征集开始日期',
    end_date        DATE NOT NULL COMMENT '征集结束日期',
    status          VARCHAR(20) DEFAULT '进行中' COMMENT '状态: 进行中/已结束',
    feedback        TEXT DEFAULT NULL COMMENT '结果反馈内容',
    feedback_time   DATETIME DEFAULT NULL COMMENT '反馈时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见征集表';

-- 5.2 意见征集-意见提交表
CREATE TABLE IF NOT EXISTS collection_opinion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_id   BIGINT NOT NULL COMMENT '关联征集ID',
    user_id         BIGINT DEFAULT NULL COMMENT '提交用户ID',
    title           VARCHAR(200) NOT NULL COMMENT '意见标题',
    real_name       VARCHAR(50) NOT NULL COMMENT '姓名',
    phone           VARCHAR(20) NOT NULL COMMENT '手机号码',
    id_card         VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    address         VARCHAR(200) DEFAULT NULL COMMENT '通信地址',
    content         TEXT NOT NULL COMMENT '留言内容',
    status          VARCHAR(20) DEFAULT '待处理' COMMENT '状态: 待处理/已回复',
    reply_content   TEXT DEFAULT NULL COMMENT '回复内容',
    reply_by        VARCHAR(50) DEFAULT NULL COMMENT '回复人',
    reply_time      DATETIME DEFAULT NULL COMMENT '回复时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人',
    deleted         INT NOT NULL DEFAULT 0 COMMENT '逻辑删除:0-未删除,1-已删除',
    INDEX idx_collection_id (collection_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见征集-意见提交表';

-- =====================================================
-- 六、政民互动 - 民意调查问卷系统
-- =====================================================

-- 6.1 问卷主表
CREATE TABLE IF NOT EXISTS questionnaire (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description     TEXT COMMENT '问卷说明',
    status          VARCHAR(20) DEFAULT '草稿' COMMENT '状态: 草稿/已发布/已关闭',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    close_time      DATETIME DEFAULT NULL COMMENT '关闭时间',
    total_answers   INT DEFAULT 0 COMMENT '答卷总数',
    create_by       VARCHAR(50) COMMENT '创建人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0
) ENGINE=InnoDB COMMENT='民意调查问卷';

-- 6.2 问卷题目表
CREATE TABLE IF NOT EXISTS questionnaire_question (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    question_text   TEXT NOT NULL COMMENT '题目内容',
    question_type   VARCHAR(20) NOT NULL COMMENT '题目类型: single/multiple/text',
    options         TEXT COMMENT '选项(JSON数组): ["选项A","选项B"]',
    required        TINYINT DEFAULT 1 COMMENT '是否必答: 0否 1是',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='问卷题目';

-- 6.3 答卷表
CREATE TABLE IF NOT EXISTS questionnaire_answer (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    user_name       VARCHAR(50) COMMENT '填写人姓名',
    phone           VARCHAR(20) COMMENT '联系电话',
    submit_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间'
) ENGINE=InnoDB COMMENT='问卷答卷';

-- 6.4 答卷详情表
CREATE TABLE IF NOT EXISTS questionnaire_answer_detail (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    answer_id       BIGINT NOT NULL COMMENT '答卷ID',
    question_id     BIGINT NOT NULL COMMENT '题目ID',
    answer_text     TEXT COMMENT '答案文本(文本题)',
    answer_options  TEXT COMMENT '答案选项(JSON数组): ["选项A","选项C"]',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='答卷详情';

-- =====================================================
-- 七、依申请公开模块
-- =====================================================

-- 7.1 依申请公开申请表（基础表 + 增强字段）
CREATE TABLE IF NOT EXISTS disclosure_apply (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no        VARCHAR(50) NOT NULL COMMENT '申请编号',
    applicant       VARCHAR(50) NOT NULL COMMENT '申请人',
    id_card         VARCHAR(20) COMMENT '身份证号',
    phone           VARCHAR(20) COMMENT '联系电话',
    content         TEXT NOT NULL COMMENT '申请内容',
    purpose         VARCHAR(500) COMMENT '用途说明',
    acquire_method  VARCHAR(50) DEFAULT NULL COMMENT '获取方式: 邮寄/电子邮件/自行领取',
    file_id         BIGINT DEFAULT NULL COMMENT '申请文件ID',
    status          VARCHAR(20) DEFAULT '已受理' COMMENT '状态: 已受理/审核中/已答复/已驳回',
    deadline        DATE COMMENT '答复期限(15个工作日)',
    reply_content   TEXT COMMENT '答复内容',
    reply_by        VARCHAR(50) COMMENT '答复人',
    reply_time      DATETIME DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by       BIGINT DEFAULT 0 COMMENT '创建人',
    update_by       BIGINT DEFAULT 0 COMMENT '更新人',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted         TINYINT DEFAULT 0,
    UNIQUE KEY uk_apply_no (apply_no)
) ENGINE=InnoDB COMMENT='依申请公开记录';

-- 7.2 保密文件库表
CREATE TABLE IF NOT EXISTS disclosure_file (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_code           VARCHAR(50) NOT NULL COMMENT '文件编号',
    file_name           VARCHAR(200) NOT NULL COMMENT '文件名称',
    file_category       VARCHAR(50) DEFAULT NULL COMMENT '文件分类',
    file_type           VARCHAR(20) DEFAULT NULL COMMENT '文件类型: pdf/doc/docx/xls/xlsx/jpg/png',
    file_size           BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_url            VARCHAR(300) DEFAULT NULL COMMENT '文件存储路径',
    description         TEXT DEFAULT NULL COMMENT '文件描述',
    dept_code           VARCHAR(50) DEFAULT NULL COMMENT '所属部门',
    dept_name           VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    confidentiality_level VARCHAR(30) DEFAULT 'secret' COMMENT '保密级别: secret/confidential/top_secret',
    status              VARCHAR(30) DEFAULT 'secret' COMMENT '状态: secret(保密)/approved(已批准公开)/public(已公开)',
    publish_time        DATETIME DEFAULT NULL COMMENT '公开时间',
    create_by           BIGINT DEFAULT NULL COMMENT '创建人ID',
    create_name         VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    update_by           BIGINT DEFAULT NULL COMMENT '更新人ID',
    update_name         VARCHAR(50) DEFAULT NULL COMMENT '更新人姓名',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    UNIQUE KEY uk_disclosure_file_code (file_code),
    INDEX idx_disclosure_file_category (file_category),
    INDEX idx_disclosure_file_status (status)
) ENGINE=InnoDB COMMENT='保密文件库表';

-- 7.3 文件访问权限表
CREATE TABLE IF NOT EXISTS disclosure_file_access (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id         BIGINT NOT NULL COMMENT '文件ID',
    apply_no        VARCHAR(50) NOT NULL COMMENT '申请编号',
    user_id         BIGINT DEFAULT NULL COMMENT '申请人ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    access_type     VARCHAR(30) DEFAULT 'view' COMMENT '访问类型: view(查看)/download(下载)',
    granted_by      BIGINT DEFAULT NULL COMMENT '授权人ID',
    granted_name    VARCHAR(50) DEFAULT NULL COMMENT '授权人姓名',
    grant_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    expire_time     DATETIME DEFAULT NULL COMMENT '过期时间',
    status          VARCHAR(30) DEFAULT 'active' COMMENT '状态: active/expired/revoked',
    INDEX idx_disclosure_file_access_file (file_id),
    INDEX idx_disclosure_file_access_apply (apply_no),
    INDEX idx_disclosure_file_access_user (user_id)
) ENGINE=InnoDB COMMENT='文件访问权限表';

-- 7.4 信息公开审核记录表
CREATE TABLE IF NOT EXISTS disclosure_audit_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    apply_no        VARCHAR(50) NOT NULL COMMENT '申请编号',
    node_name       VARCHAR(100) NOT NULL COMMENT '节点名称',
    operator_id     BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name   VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    action          VARCHAR(30) DEFAULT NULL COMMENT '操作: approve/reject/transfer',
    opinion         VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    operate_time    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_disclosure_audit_apply (apply_no)
) ENGINE=InnoDB COMMENT='信息公开审核记录表';

-- =====================================================
-- 脚本结束
-- =====================================================

