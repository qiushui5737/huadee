-- =====================================================
-- V007: 咨询表增加 user_id 字段，关联提交用户
-- 原因: 查询进度时需校验归属，防止隐私泄露
-- 日期: 2026-07-15
-- =====================================================

ALTER TABLE consultation ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '提交用户ID' AFTER id;
CREATE INDEX idx_user_id ON consultation(user_id);
