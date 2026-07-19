-- =====================================================
-- V002: message表增强 - 留言类型、评价、督办字段
-- 作者: dev-interaction
-- 日期: 2026-07-16
-- =====================================================

ALTER TABLE message
    ADD COLUMN type VARCHAR(20) DEFAULT '咨询' COMMENT '留言类型: 咨询/投诉/建议/求助' AFTER title,
    ADD COLUMN rating TINYINT DEFAULT NULL COMMENT '群众评价: 1-5星' AFTER reply_time,
    ADD COLUMN rating_content TEXT DEFAULT NULL COMMENT '评价内容' AFTER rating,
    ADD COLUMN supervise TINYINT DEFAULT 0 COMMENT '是否督办: 0否 1是' AFTER status,
    ADD COLUMN supervise_time DATETIME DEFAULT NULL COMMENT '督办时间' AFTER supervise,
    ADD COLUMN deadline DATETIME DEFAULT NULL COMMENT '处理期限' AFTER supervise_time;
