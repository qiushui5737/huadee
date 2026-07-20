-- =====================================================
-- V006: 咨询表 attachment 字段扩容为 LONGTEXT
-- 原因: 图片以 base64 编码存储，VARCHAR(500) 容量不足导致数据截断
-- 日期: 2026-07-15
-- =====================================================

ALTER TABLE consultation MODIFY COLUMN attachment LONGTEXT COMMENT '附件(JSON格式,含base64图片数据)';
