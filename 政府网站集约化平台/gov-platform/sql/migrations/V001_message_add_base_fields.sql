-- =====================================================
-- V001: message表补充BaseEntity公共字段
-- 说明: MyBatis-Plus自动填充需要update_time/create_by/update_by
-- 作者: dev-interaction
-- 日期: 2026-07-16
-- =====================================================

ALTER TABLE message
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ADD COLUMN create_by   BIGINT   DEFAULT 0 COMMENT '创建人',
    ADD COLUMN update_by   BIGINT   DEFAULT 0 COMMENT '更新人';
