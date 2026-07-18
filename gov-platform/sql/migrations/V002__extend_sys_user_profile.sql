USE gov_platform;

ALTER TABLE sys_user
    ADD COLUMN gender VARCHAR(10) NULL COMMENT '性别' AFTER real_name,
    ADD COLUMN id_card VARCHAR(18) NULL COMMENT '身份证号' AFTER gender,
    ADD COLUMN email VARCHAR(100) NULL COMMENT '邮箱' AFTER phone,
    ADD COLUMN address VARCHAR(255) NULL COMMENT '联系地址' AFTER dept_code,
    ADD UNIQUE KEY uk_id_card (id_card);
