-- =====================================================
-- V015: 补充 message 和 disclosure_apply 表的 BaseEntity 字段
-- 作者: dev-interaction
-- 日期: 2026-07-18
-- 说明: message 表缺少 create_by/update_by/deleted
--       disclosure_apply 表缺少 deleted
-- =====================================================

USE gov_platform;

-- message 表补充 BaseEntity 字段
ALTER TABLE message
    ADD COLUMN IF NOT EXISTS create_by BIGINT DEFAULT 0 COMMENT '创建人' AFTER deleted,
    ADD COLUMN IF NOT EXISTS update_by BIGINT DEFAULT 0 COMMENT '更新人' AFTER create_by;

-- disclosure_apply 表补充 deleted 字段
ALTER TABLE disclosure_apply
    ADD COLUMN IF NOT EXISTS deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除' AFTER update_by;

