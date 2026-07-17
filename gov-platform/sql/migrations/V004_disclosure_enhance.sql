-- =====================================================
-- V004: disclosure_apply表增强 - 获取方式、时限管理字段
-- 作者: dev-interaction
-- 日期: 2026-07-16
-- =====================================================

ALTER TABLE disclosure_apply
    ADD COLUMN acquire_method VARCHAR(50) DEFAULT NULL COMMENT '获取方式: 邮寄/电子邮件/自行领取' AFTER purpose,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER reply_time,
    ADD COLUMN create_by BIGINT DEFAULT 0 COMMENT '创建人' AFTER update_time,
    ADD COLUMN update_by BIGINT DEFAULT 0 COMMENT '更新人' AFTER create_by;
