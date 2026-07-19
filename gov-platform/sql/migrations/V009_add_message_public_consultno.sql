-- V009: 留言表增加公开标记和信件单号
ALTER TABLE message ADD COLUMN is_public TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否公开: 1-公开, 0-私密' AFTER rating_content;
ALTER TABLE message ADD COLUMN consult_no VARCHAR(32) DEFAULT NULL COMMENT '信件单号' AFTER is_public;
CREATE UNIQUE INDEX idx_consult_no ON message(consult_no);
