CREATE TABLE IF NOT EXISTS service_inquiry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    accept_no VARCHAR(50) NOT NULL COMMENT '受理号',
    content TEXT NOT NULL COMMENT '询问内容',
    reply TEXT COMMENT '回复内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '询问时间',
    reply_time DATETIME COMMENT '回复时间',
    KEY idx_accept_no (accept_no)
) ENGINE=InnoDB COMMENT='办事询问记录';