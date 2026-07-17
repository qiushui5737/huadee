-- =====================================================
-- V003: 民意调查问卷系统 - 新建表
-- 作者: dev-interaction
-- 日期: 2026-07-16
-- =====================================================

-- 问卷主表
CREATE TABLE IF NOT EXISTS questionnaire (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT         COMMENT '问卷说明',
    status      VARCHAR(20)  DEFAULT '草稿' COMMENT '状态: 草稿/已发布/已关闭',
    publish_time DATETIME    DEFAULT NULL COMMENT '发布时间',
    close_time  DATETIME    DEFAULT NULL COMMENT '关闭时间',
    total_answers INT       DEFAULT 0 COMMENT '答卷总数',
    create_by   VARCHAR(50)  COMMENT '创建人',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      DEFAULT 0
) ENGINE=InnoDB COMMENT='民意调查问卷';

-- 问卷题目表
CREATE TABLE IF NOT EXISTS questionnaire_question (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    question_text  TEXT NOT NULL COMMENT '题目内容',
    question_type  VARCHAR(20) NOT NULL COMMENT '题目类型: single/multiple/text',
    options        TEXT COMMENT '选项(JSON数组): ["选项A","选项B"]',
    required       TINYINT DEFAULT 1 COMMENT '是否必答: 0否 1是',
    sort_order     INT DEFAULT 0 COMMENT '排序',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='问卷题目';

-- 答卷表
CREATE TABLE IF NOT EXISTS questionnaire_answer (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    user_name   VARCHAR(50)  COMMENT '填写人姓名',
    phone       VARCHAR(20)  COMMENT '联系电话',
    submit_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间'
) ENGINE=InnoDB COMMENT='问卷答卷';

-- 答卷详情表
CREATE TABLE IF NOT EXISTS questionnaire_answer_detail (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    answer_id   BIGINT NOT NULL COMMENT '答卷ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    answer_text TEXT COMMENT '答案文本(文本题)',
    answer_options TEXT COMMENT '答案选项(JSON数组): ["选项A","选项C"]',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='答卷详情';
