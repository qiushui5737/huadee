-- V006: 完善办事服务流程
-- service_record 扩展
ALTER TABLE service_record ADD COLUMN draft INT DEFAULT 0 COMMENT '是否草稿';
ALTER TABLE service_record ADD COLUMN current_stage VARCHAR(20) DEFAULT '窗口受理' COMMENT '当前审批阶段';
ALTER TABLE service_record ADD COLUMN supplement_reason TEXT COMMENT '补充材料原因';
ALTER TABLE service_record ADD COLUMN supplement_status VARCHAR(10) DEFAULT '无' COMMENT '补充材料状态';
ALTER TABLE service_record ADD COLUMN reject_reason TEXT COMMENT '驳回原因';
ALTER TABLE service_record ADD COLUMN rating INT COMMENT '服务评价1-5星';
ALTER TABLE service_record ADD COLUMN rating_content TEXT COMMENT '评价内容';

-- service_item 扩展
ALTER TABLE service_item ADD COLUMN conditions TEXT COMMENT '办理条件';
ALTER TABLE service_item ADD COLUMN time_limit VARCHAR(50) COMMENT '办理时限';
ALTER TABLE service_item ADD COLUMN process_desc TEXT COMMENT '办理流程说明';

-- 服务附件表
CREATE TABLE IF NOT EXISTS service_attachment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  accept_no VARCHAR(50) NOT NULL,
  file_name VARCHAR(200) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_type VARCHAR(20),
  file_size BIGINT,
  upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_accept_no (accept_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 服务评价表
CREATE TABLE IF NOT EXISTS service_rating (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  accept_no VARCHAR(50) NOT NULL,
  user_id BIGINT,
  rating INT NOT NULL,
  content TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_accept_no (accept_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
