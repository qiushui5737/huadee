CREATE DATABASE IF NOT EXISTS gov_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

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

INSERT IGNORE INTO sys_dept (dept_code, dept_name, parent_id, sort_order) VALUES
  ('ADMIN', '平台管理部门', 0, 1),
  ('EDU', '教育部门', 0, 2),
  ('HEA', '卫生健康部门', 0, 3),
  ('MCA', '民政部门', 0, 4),
  ('HRSS', '人社部门', 0, 5);

INSERT IGNORE INTO sys_user (username, password, real_name, dept_code) VALUES
  ('admin', '$2a$10$placeholder', '系统管理员', 'ADMIN'),
  ('edu_user', '$2a$10$placeholder', '教育部门专员', 'EDU'),
  ('hea_user', '$2a$10$placeholder', '卫健部门专员', 'HEA'),
  ('service_user', '$2a$10$placeholder', '办事审批专员', 'HRSS'),
  ('cms_user', '$2a$10$placeholder', '内容发布专员', 'ADMIN');

INSERT IGNORE INTO sys_role (role_code, role_name, description) VALUES
  ('ADMIN', '系统管理员', '拥有平台全部管理权限'),
  ('DEPT_ADMIN', '部门管理员', '管理本部门内容与业务'),
  ('OPERATOR', '业务操作员', '处理办件、留言、内容审核等业务');

INSERT IGNORE INTO sensitive_word (word, category, level) VALUES
  ('测试敏感词', '测试', 1),
  ('违法', '合规', 2),
  ('诈骗', '合规', 3);

INSERT IGNORE INTO service_category (category_code, category_name, sort_order) VALUES
  ('education', '教育服务', 1),
  ('health', '医疗健康', 2),
  ('social_security', '社保民政', 3);

INSERT IGNORE INTO service_item
  (item_code, item_name, category, dept_code, description, guide_text, materials, form_schema, fee_required, license_enabled)
VALUES
  ('SVC_EDU_001', '学生资助申请', 'education', 'EDU', '面向符合条件学生的资助申请事项', '在线填写申请信息并提交证明材料', '身份证明, 学籍证明, 家庭情况材料',
   JSON_OBJECT('type','object','required',JSON_ARRAY('name','idCard','school'),'properties',JSON_OBJECT('name',JSON_OBJECT('type','string','title','姓名'),'idCard',JSON_OBJECT('type','string','title','身份证号'),'school',JSON_OBJECT('type','string','title','学校'))), 0, 1),
  ('SVC_HEA_001', '健康证明办理', 'health', 'HEA', '群众健康证明在线申请', '提交基础信息后由卫健部门审核', '身份证明, 体检报告',
   JSON_OBJECT('type','object','required',JSON_ARRAY('name','phone'),'properties',JSON_OBJECT('name',JSON_OBJECT('type','string','title','姓名'),'phone',JSON_OBJECT('type','string','title','联系电话'))), 1, 1),
  ('SVC_HRSS_001', '社保参保证明打印', 'social_security', 'HRSS', '在线申请社保参保证明', '提交申请后生成电子证明', '身份证明',
   JSON_OBJECT('type','object','required',JSON_ARRAY('name','idCard'),'properties',JSON_OBJECT('name',JSON_OBJECT('type','string','title','姓名'),'idCard',JSON_OBJECT('type','string','title','身份证号'))), 0, 1);

INSERT IGNORE INTO cms_site (site_code, site_name, dept_code, template) VALUES
  ('main', '政府门户主站', 'ADMIN', 'default'),
  ('edu', '教育部门子站', 'EDU', 'department'),
  ('health', '卫健部门子站', 'HEA', 'department');

INSERT IGNORE INTO cms_channel (site_code, channel_code, channel_name, sort_order) VALUES
  ('main', 'notice', '通知公告', 1),
  ('main', 'policy', '政策文件', 2),
  ('edu', 'edu_news', '教育动态', 1),
  ('health', 'health_news', '健康资讯', 1);

INSERT IGNORE INTO cms_content (site_code, channel_code, title, summary, content, category, author, status, publish_time) VALUES
  ('main', 'notice', '政府网站集约化平台上线试运行', '平台提供办事服务、政民互动、信息公开等能力。', '政府网站集约化平台上线试运行，欢迎群众体验在线服务。', '通知', '系统管理员', 'published', NOW()),
  ('main', 'policy', '优化政务服务办理流程的通知', '进一步提升网上办事效率。', '各部门应推进事项标准化、表单电子化、审批流程线上化。', '政策', '系统管理员', 'published', NOW());

INSERT IGNORE INTO disclosure_catalog (code, name, parent_id, level, sort_order, guide_text) VALUES
  ('GKZN', '政府信息公开指南', 0, 1, 1, '说明政府信息公开申请方式、受理流程和答复时限。'),
  ('GKML', '法定主动公开内容', 0, 1, 2, '部门职责、政策文件、财政信息、服务事项等公开目录。'),
  ('GKND', '政府信息公开年报', 0, 1, 3, '年度政府信息公开工作报告。');

INSERT IGNORE INTO knowledge_doc (title, content, source, dept_code, doc_type, status) VALUES
  ('学生资助申请办理指南', '申请人可在线提交姓名、身份证号、学校和证明材料，教育部门审核后反馈结果。', '办事服务', 'EDU', 'guide', 1),
  ('健康证明办理指南', '群众提交基础信息和体检报告后，由卫生健康部门审核并出具电子证明。', '办事服务', 'HEA', 'guide', 1),
  ('政府信息公开申请说明', '申请人可在线提交政府信息公开申请，平台按照受理、审核、答复流程办理。', '信息公开', 'ADMIN', 'guide', 1);

INSERT IGNORE INTO performance_metric (dept_code, metric_code, metric_name, target_value, actual_value, score, weight, period) VALUES
  ('EDU', 'reply_rate', '留言回复率', 95.00, 98.00, 98.00, 1.00, '2026-Q3'),
  ('HEA', 'service_time', '办件按时率', 95.00, 96.50, 96.50, 1.00, '2026-Q3'),
  ('HRSS', 'satisfaction', '群众满意度', 90.00, 94.00, 94.00, 1.00, '2026-Q3');

INSERT IGNORE INTO report_stat (stat_date, stat_type, stat_key, stat_value) VALUES
  (CURDATE(), 'visit', 'total', 1284567),
  (CURDATE(), 'service', 'total', 3568),
  (CURDATE(), 'message', 'total', 8923),
  (CURDATE(), 'content', 'published', 1260);
