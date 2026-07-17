-- 变更编号：VYYYYMMDD_00
-- 负责人：A/B/C/D/E
-- 影响模块：填写模块名称
-- 变更说明：填写本次数据库变更原因
-- 执行库：gov_platform

USE gov_platform;

-- 示例1：新增字段
-- ALTER TABLE service_record
--   ADD COLUMN handle_dept VARCHAR(50) NULL COMMENT '办理部门';

-- 示例2：新增索引
-- ALTER TABLE service_record
--   ADD INDEX idx_service_record_status (status);

-- 示例3：新增表
-- CREATE TABLE IF NOT EXISTS demo_table (
--   id BIGINT PRIMARY KEY AUTO_INCREMENT,
--   demo_name VARCHAR(100) NOT NULL,
--   status VARCHAR(30) DEFAULT 'enabled',
--   create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
--   update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--   deleted TINYINT DEFAULT 0
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

