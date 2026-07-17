
-- Unified schema for gov_platform.
-- Purpose: create the shared database and all project tables for every team member.
-- Safe to run repeatedly: uses CREATE DATABASE IF NOT EXISTS and CREATE TABLE IF NOT EXISTS.
-- Run from this sql directory:
--   mysql -uroot -p050420 < gov_platform_all_tables.sql

CREATE DATABASE IF NOT EXISTS gov_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE gov_platform;

CREATE TABLE IF NOT EXISTS sys_dept (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_code VARCHAR(50) NOT NULL,
  dept_name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  leader VARCHAR(50),
  phone VARCHAR(30),
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_sys_dept_code (dept_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(200) NOT NULL,
  real_name VARCHAR(50),
  phone VARCHAR(30),
  email VARCHAR(100),
  dept_code VARCHAR(50),
  status TINYINT DEFAULT 1,
  last_login_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_dept (dept_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(100) NOT NULL,
  perm_name VARCHAR(100) NOT NULL,
  perm_type VARCHAR(20) DEFAULT 'menu',
  parent_id BIGINT DEFAULT 0,
  path VARCHAR(200),
  component VARCHAR(200),
  sort_order INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_permission_code (perm_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_role_permission (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sys_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  type VARCHAR(30) DEFAULT 'system',
  title VARCHAR(200),
  content TEXT,
  business_type VARCHAR(50),
  business_id VARCHAR(80),
  channel VARCHAR(30) DEFAULT 'site',
  send_status VARCHAR(20) DEFAULT 'pending',
  is_read TINYINT DEFAULT 0,
  read_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_sys_message_user (user_id, is_read)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_code VARCHAR(50) NOT NULL,
  template_name VARCHAR(100) NOT NULL,
  channel VARCHAR(30) NOT NULL,
  content TEXT NOT NULL,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_notification_template_code (template_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS knowledge_doc (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content LONGTEXT,
  source VARCHAR(200),
  dept_code VARCHAR(50),
  doc_type VARCHAR(50),
  status TINYINT DEFAULT 1,
  es_doc_id VARCHAR(100),
  vector_status TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  KEY idx_knowledge_doc_dept (dept_code),
  FULLTEXT KEY ft_knowledge_doc_title_content (title, content)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS search_index_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(50) NOT NULL,
  biz_id BIGINT NOT NULL,
  operation VARCHAR(20) NOT NULL,
  status VARCHAR(20) DEFAULT 'pending',
  error_msg TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  finish_time DATETIME,
  KEY idx_search_index_task_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS vector_embedding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(50) NOT NULL,
  biz_id BIGINT NOT NULL,
  chunk_no INT DEFAULT 0,
  chunk_text TEXT NOT NULL,
  embedding LONGTEXT,
  model_name VARCHAR(100),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_vector_embedding_biz (biz_type, biz_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sensitive_word (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  word VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  level TINYINT DEFAULT 1,
  match_type VARCHAR(20) DEFAULT 'dfa',
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_sensitive_word_word (word)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL,
  user_id BIGINT,
  question TEXT NOT NULL,
  answer TEXT,
  sources TEXT,
  model_name VARCHAR(100),
  audit_status VARCHAR(20) DEFAULT 'normal',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_chat_session_session (session_id),
  KEY idx_chat_session_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ai_conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(64) NOT NULL,
  user_id BIGINT,
  title VARCHAR(200),
  memory_summary TEXT,
  last_message_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ai_conversation_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(50) NOT NULL,
  category_name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  UNIQUE KEY uk_service_category_code (category_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_code VARCHAR(50) NOT NULL,
  item_name VARCHAR(200) NOT NULL,
  category VARCHAR(50),
  dept_code VARCHAR(50),
  description TEXT,
  guide_text TEXT,
  materials TEXT,
  form_schema JSON,
  flow_key VARCHAR(80),
  fee_required TINYINT DEFAULT 0,
  license_enabled TINYINT DEFAULT 0,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  UNIQUE KEY uk_service_item_code (item_code),
  KEY idx_service_item_dept (dept_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS form_definition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  form_code VARCHAR(50) NOT NULL,
  form_name VARCHAR(100) NOT NULL,
  item_id BIGINT,
  schema_json JSON NOT NULL,
  ui_schema JSON,
  version INT DEFAULT 1,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_form_definition_code_version (form_code, version)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS service_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  accept_no VARCHAR(50) NOT NULL,
  item_id BIGINT NOT NULL,
  user_id BIGINT,
  user_name VARCHAR(50),
  phone VARCHAR(30),
  form_data JSON,
  status VARCHAR(30) DEFAULT 'accepted',
  current_node VARCHAR(100),
  submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  finish_time DATETIME,
  UNIQUE KEY uk_service_record_accept_no (accept_no),
  KEY idx_service_record_item (item_id),
  KEY idx_service_record_user (user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workflow_definition (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_key VARCHAR(80) NOT NULL,
  flow_name VARCHAR(100) NOT NULL,
  bpmn_xml LONGTEXT,
  version INT DEFAULT 1,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_workflow_definition_key_version (flow_key, version)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS approval_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  accept_no VARCHAR(50) NOT NULL,
  task_name VARCHAR(100) NOT NULL,
  assignee_id BIGINT,
  assignee_name VARCHAR(50),
  action VARCHAR(30),
  opinion VARCHAR(500),
  status VARCHAR(30) DEFAULT 'pending',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  finish_time DATETIME,
  KEY idx_approval_task_accept (accept_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL,
  accept_no VARCHAR(50) NOT NULL,
  amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  pay_channel VARCHAR(30),
  pay_status VARCHAR(30) DEFAULT 'unpaid',
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_payment_order_no (order_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS electronic_license (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  license_no VARCHAR(80) NOT NULL,
  accept_no VARCHAR(50) NOT NULL,
  license_name VARCHAR(100) NOT NULL,
  holder_name VARCHAR(50),
  issue_dept VARCHAR(50),
  issue_time DATETIME,
  expire_time DATETIME,
  file_url VARCHAR(300),
  status VARCHAR(30) DEFAULT 'valid',
  UNIQUE KEY uk_electronic_license_no (license_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  user_name VARCHAR(50),
  phone VARCHAR(30),
  type VARCHAR(30) DEFAULT 'consult',
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  dept_code VARCHAR(50),
  status VARCHAR(30) DEFAULT 'pending_assign',
  satisfaction_score TINYINT,
  satisfaction_comment VARCHAR(500),
  reply_content TEXT,
  reply_by VARCHAR(50),
  reply_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  KEY idx_message_status (status),
  KEY idx_message_dept (dept_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message_assignment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  from_dept VARCHAR(50),
  to_dept VARCHAR(50) NOT NULL,
  assignee_id BIGINT,
  assign_reason VARCHAR(500),
  assign_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) DEFAULT 'assigned',
  KEY idx_message_assignment_message (message_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS message_supervision (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  message_id BIGINT NOT NULL,
  deadline DATETIME NOT NULL,
  remind_count INT DEFAULT 0,
  overdue TINYINT DEFAULT 0,
  supervise_status VARCHAR(30) DEFAULT 'normal',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_message_supervision_message (message_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS disclosure_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_no VARCHAR(50) NOT NULL,
  applicant VARCHAR(50) NOT NULL,
  applicant_type VARCHAR(20) DEFAULT 'person',
  id_card VARCHAR(30),
  phone VARCHAR(30),
  email VARCHAR(100),
  address VARCHAR(300),
  content TEXT NOT NULL,
  purpose VARCHAR(500),
  obtain_method VARCHAR(50),
  status VARCHAR(30) DEFAULT 'accepted',
  deadline DATE,
  reply_content TEXT,
  reply_file_url VARCHAR(300),
  reply_by VARCHAR(50),
  reply_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_disclosure_apply_no (apply_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS disclosure_audit_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_no VARCHAR(50) NOT NULL,
  node_name VARCHAR(100) NOT NULL,
  operator_id BIGINT,
  operator_name VARCHAR(50),
  action VARCHAR(30),
  opinion VARCHAR(500),
  operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_disclosure_audit_apply (apply_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS survey (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_code VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  status VARCHAR(30) DEFAULT 'draft',
  start_time DATETIME,
  end_time DATETIME,
  create_by BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_survey_code (survey_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS survey_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  question_type VARCHAR(30) NOT NULL,
  title VARCHAR(300) NOT NULL,
  options_json JSON,
  required TINYINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  KEY idx_survey_question_survey (survey_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS survey_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  survey_id BIGINT NOT NULL,
  user_id BIGINT,
  answer_json JSON NOT NULL,
  submit_ip VARCHAR(50),
  submit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_survey_answer_survey (survey_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_site (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  site_code VARCHAR(50) NOT NULL,
  site_name VARCHAR(100) NOT NULL,
  dept_code VARCHAR(50),
  template VARCHAR(50) DEFAULT 'default',
  logo_url VARCHAR(300),
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_cms_site_code (site_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_channel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  site_code VARCHAR(50) NOT NULL,
  channel_code VARCHAR(50) NOT NULL,
  channel_name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  sort_order INT DEFAULT 0,
  status TINYINT DEFAULT 1,
  UNIQUE KEY uk_cms_channel_site_code (site_code, channel_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  site_code VARCHAR(50) NOT NULL,
  channel_code VARCHAR(50),
  title VARCHAR(300) NOT NULL,
  summary VARCHAR(500),
  content LONGTEXT,
  category VARCHAR(50),
  author VARCHAR(50),
  cover_url VARCHAR(300),
  status VARCHAR(30) DEFAULT 'pending_review',
  audit_level TINYINT DEFAULT 0,
  publish_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  KEY idx_cms_content_site_channel (site_code, channel_code),
  FULLTEXT KEY ft_cms_content_title_content (title, content)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cms_audit_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content_id BIGINT NOT NULL,
  audit_level TINYINT NOT NULL,
  auditor_id BIGINT,
  auditor_name VARCHAR(50),
  result VARCHAR(30) NOT NULL,
  opinion VARCHAR(500),
  audit_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_cms_audit_content (content_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS disclosure_catalog (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50),
  level TINYINT DEFAULT 1,
  sort_order INT DEFAULT 0,
  guide_text TEXT,
  annual_report_url VARCHAR(300),
  UNIQUE KEY uk_disclosure_catalog_code (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS access_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  session_id VARCHAR(80),
  page_url VARCHAR(500),
  event_type VARCHAR(50),
  event_data JSON,
  ip VARCHAR(50),
  user_agent VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_access_log_time (create_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS etl_job (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  job_code VARCHAR(50) NOT NULL,
  job_name VARCHAR(100) NOT NULL,
  cron_expr VARCHAR(50),
  last_run_time DATETIME,
  last_status VARCHAR(30),
  enabled TINYINT DEFAULT 1,
  UNIQUE KEY uk_etl_job_code (job_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS report_stat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stat_date DATE NOT NULL,
  stat_type VARCHAR(50) NOT NULL,
  stat_key VARCHAR(80) NOT NULL,
  stat_value DECIMAL(18,2) DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_report_stat (stat_date, stat_type, stat_key)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS performance_metric (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dept_code VARCHAR(50) NOT NULL,
  metric_code VARCHAR(50) NOT NULL,
  metric_name VARCHAR(100) NOT NULL,
  target_value DECIMAL(10,2),
  actual_value DECIMAL(10,2),
  score DECIMAL(5,2),
  weight DECIMAL(5,2) DEFAULT 1.00,
  period VARCHAR(20),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_performance_metric (dept_code, metric_code, period)
) ENGINE=InnoDB;

-- =====================================================
-- V003: 民意调查问卷系统 - 新建表
-- 作者: dev-interaction
-- 日期: 2026-07-16
-- =====================================================

-- 问卷主表
CREATE TABLE IF NOT EXISTS questionnaire (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT         COMMENT '问卷说明',
    status      VARCHAR(20)  DEFAULT '草稿' COMMENT '状态: 草稿/已发布/已关闭',
    publish_time DATETIME    DEFAULT NULL COMMENT '发布时间',
    close_time  DATETIME    DEFAULT NULL COMMENT '关闭时间',
    total_answers INT       DEFAULT 0 COMMENT '答卷总数',
    create_by   VARCHAR(50)  COMMENT '创建人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB COMMENT='民意调查问卷';

-- 问卷题目表
CREATE TABLE IF NOT EXISTS questionnaire_question (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    question_text  TEXT NOT NULL COMMENT '题目内容',
    question_type  VARCHAR(20) NOT NULL COMMENT '题目类型: single/multiple/text',
    options        TEXT COMMENT '选项(JSON数组): ["选项A","选项B"]',
    required       TINYINT DEFAULT 1 COMMENT '是否必答: 0否 1是',
    sort_order     INT DEFAULT 0 COMMENT '排序',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='问卷题目';

-- 答卷表
CREATE TABLE IF NOT EXISTS questionnaire_answer (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    user_name   VARCHAR(50)  COMMENT '填写人姓名',
    phone       VARCHAR(20)  COMMENT '联系电话',
    submit_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间'
) ENGINE=InnoDB COMMENT='问卷答卷';

-- 答卷详情表
CREATE TABLE IF NOT EXISTS questionnaire_answer_detail (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    answer_id   BIGINT NOT NULL COMMENT '答卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer_text TEXT COMMENT '答案文本(文本题)',
    answer_options TEXT COMMENT '答案选项(JSON数组): ["选项A","选项C"]',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='答卷详情';

-- =====================================================
-- Project compatibility: authentication, profile and current entities
-- Safe for both a fresh database and an existing shared database.
-- =====================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS add_column_if_missing$$
CREATE PROCEDURE add_column_if_missing(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE \`', p_table, '\` ADD COLUMN \`', p_column, '\` ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

DROP PROCEDURE IF EXISTS add_index_if_missing$$
CREATE PROCEDURE add_index_if_missing(
  IN p_table VARCHAR(64),
  IN p_index VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE \`', p_table, '\` ADD ', p_definition);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$

DELIMITER ;

CALL add_column_if_missing('sys_user', 'gender', 'VARCHAR(10) NULL COMMENT ''性别'' AFTER \`real_name\`');
CALL add_column_if_missing('sys_user', 'id_card', 'VARCHAR(18) NULL COMMENT ''身份证号'' AFTER \`gender\`');
CALL add_column_if_missing('sys_user', 'address', 'VARCHAR(255) NULL COMMENT ''联系地址'' AFTER \`dept_code\`');
CALL add_index_if_missing('sys_user', 'uk_id_card', 'UNIQUE KEY \`uk_id_card\` (\`id_card\`)');

CALL add_column_if_missing('message', 'supervise', 'TINYINT DEFAULT 0 COMMENT ''是否督办'' AFTER \`status\`');
CALL add_column_if_missing('message', 'supervise_time', 'DATETIME NULL COMMENT ''督办时间'' AFTER \`supervise\`');
CALL add_column_if_missing('message', 'deadline', 'DATETIME NULL COMMENT ''处理期限'' AFTER \`supervise_time\`');
CALL add_column_if_missing('message', 'rating', 'TINYINT NULL COMMENT ''评价星级1-5'' AFTER \`reply_time\`');
CALL add_column_if_missing('message', 'rating_content', 'TEXT NULL COMMENT ''评价内容'' AFTER \`rating\`');
CALL add_column_if_missing('message', 'create_by', 'BIGINT DEFAULT 0 COMMENT ''创建人''');
CALL add_column_if_missing('message', 'update_by', 'BIGINT DEFAULT 0 COMMENT ''更新人''');

CALL add_column_if_missing('disclosure_apply', 'acquire_method', 'VARCHAR(50) NULL COMMENT ''获取方式'' AFTER \`purpose\`');
CALL add_column_if_missing('disclosure_apply', 'update_time', 'DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''');
CALL add_column_if_missing('disclosure_apply', 'create_by', 'BIGINT DEFAULT 0 COMMENT ''创建人''');
CALL add_column_if_missing('disclosure_apply', 'update_by', 'BIGINT DEFAULT 0 COMMENT ''更新人''');
CALL add_column_if_missing('disclosure_apply', 'deleted', 'TINYINT DEFAULT 0 COMMENT ''逻辑删除''');

DROP PROCEDURE IF EXISTS add_index_if_missing;
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- Required role data. Existing role IDs and descriptions are preserved.
INSERT INTO sys_role (role_code, role_name, description)
VALUES
  ('ADMIN', '系统管理员', '平台全局管理员'),
  ('DEPT_ADMIN', '部门管理员', '部门业务管理员'),
  ('OPERATOR', '普通用户', '门户注册用户')
ON DUPLICATE KEY UPDATE
  role_name = VALUES(role_name);

-- Initial administrator is inserted only when it does not already exist.
-- Default password: Admin@123
INSERT INTO sys_user (username, password, real_name, dept_code, status, deleted)
SELECT 'admin',
       '$2b$10$BjPh4TqxXwz4bHenk/t8x.qqyyZQ7ILlsQZwlq04itIU3hlzdr2jq',
       '系统管理员', 'ADMIN', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin';

