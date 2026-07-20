-- ============================================
-- 依申请公开板块 测试数据批量插入
-- ============================================

USE gov_platform;

-- 1. 补充更多保密文件（如有缺失的分类）
INSERT INTO disclosure_file (file_code, file_name, file_category, dept_code, dept_name, description, confidentiality_level, file_url, file_size, file_type, status, deleted, create_time) VALUES
('DF005', '2025年度社会保障基金审计报告', 'finance', 'FIN', '财政局', '2025年度社会保障基金的收支、管理和投资运营情况的审计结果', 'secret', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 30 DAY),
('DF006', '城市交通基础设施建设规划', 'planning', 'TRANS', '交通运输局', '2026-2035年城市交通基础设施建设总体规划和重点项目安排', 'internal', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 25 DAY),
('DF007', '2025年环境保护工作总结', 'other', 'ENV', '生态环境局', '2025年度全市环境保护工作完成情况、存在问题和下一步工作计划', 'public', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 20 DAY),
('DF008', '医疗卫生资源配置专项规划', 'planning', 'HEALTH', '卫健委', '2026-2030年医疗卫生资源优化配置方案，包括新建医院、社区卫生服务中心等', 'secret', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 15 DAY),
('DF009', '2026年政府采购目录及标准', 'finance', 'FIN', '财政局', '2026年度政府集中采购目录及采购限额标准', 'internal', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 10 DAY),
('DF010', '教育均衡发展专项资金管理办法', 'policy', 'EDU', '教育局', '关于规范教育均衡发展专项资金使用管理的实施办法', 'secret', NULL, 0, NULL, 'published', 0, NOW() - INTERVAL 5 DAY);

-- 2. 插入多条申请记录（不同申请人、不同状态、不同文件）
INSERT INTO disclosure_apply (file_id, apply_no, applicant, id_card, phone, content, purpose, acquire_method, status, deadline, reply_content, reply_by, reply_time, create_time, deleted) VALUES
-- 李四 - 已答复（审核通过）
(2, 'YA100001', '李四', '510105199203151234', '13900139001', '申请公开城市发展规划2026-2030全文', '学术研究', '电子邮件', '已答复', '2026-08-15', '经审核，该规划文件已脱密处理，同意公开。', '王主任', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 12 DAY, 0),
-- 王五 - 已驳回
(3, 'YA100002', '王五', '510107198807203456', '13700137002', '申请查看教育政策实施细则中涉及收费标准的章节', '个人维权', '邮寄', '已驳回', '2026-08-16', '该文件涉及敏感收费信息，根据相关规定暂不予公开，建议向教育局咨询具体收费标准。', '赵科长', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 10 DAY, 0),
-- 赵六 - 审核中
(5, 'YA100003', '赵六', '510104199505107890', '13600136003', '申请公开社会保障基金审计报告中的养老保险部分', '社保研究', '自行领取', '审核中', '2026-08-20', NULL, NULL, NULL, NOW() - INTERVAL 5 DAY, 0),
-- 孙七 - 已受理
(6, 'YA100004', '孙七', '510106200001012345', '13500135004', '申请公开城市交通基础设施建设规划中地铁线路规划部分', '出行参考', '电子邮件', '已受理', '2026-08-22', NULL, NULL, NULL, NOW() - INTERVAL 3 DAY, 0),
-- 周八 - 已答复（审核通过）
(7, 'YA100005', '周八', '510108197812156789', '13400134005', '申请公开2025年环境保护工作总结报告', '环保志愿者', '电子邮件', '已答复', '2026-08-18', '经审核，该报告不涉及国家秘密和商业秘密，同意公开。', '李处长', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 8 DAY, 0),
-- 吴九 - 已答复（审核通过）
(8, 'YA100006', '吴九', '510102198506089012', '13300133006', '申请公开医疗卫生资源配置专项规划全文', '医疗行业从业者', '邮寄', '已答复', '2026-08-19', '该规划经脱密审查后可予公开，请携带身份证件到卫健委领取纸质版。', '张主任', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 7 DAY, 0),
-- 郑十 - 已驳回
(9, 'YA100007', '郑十', '510103199109023456', '13200132007', '申请公开2026年政府采购目录中涉及信息化项目的详细清单', '商业参考', '电子邮件', '已驳回', '2026-08-21', '采购目录中涉及供应商商业秘密的部分不予公开，公开版已在政府采购网站发布。', '刘科长', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 9 DAY, 0),
-- 陈十一 - 审核中
(10, 'YA100008', '陈十一', '510109199803254567', '13100131008', '申请查看教育均衡发展专项资金管理办法中资金分配标准', '教育研究', '电子邮件', '审核中', '2026-08-25', NULL, NULL, NULL, NOW() - INTERVAL 1 DAY, 0),
-- 李四第二次申请
(4, 'YA100009', '李四', '510105199203151234', '13900139001', '申请公开2026年政府采购目录及标准', '学术研究', '电子邮件', '已受理', '2026-08-26', NULL, NULL, NULL, NOW() - INTERVAL 0 DAY, 0),
-- 张三第三次申请
(8, 'YA100010', '张三', '110101199001011234', '13800138000', '申请公开医疗卫生资源配置专项规划中社区卫生服务中心布局部分', '社区调研', '自行领取', '审核中', '2026-08-28', NULL, NULL, NULL, NOW() - INTERVAL 0 DAY, 0);

-- 3. 为已答复通过的申请插入访问权限记录
INSERT INTO disclosure_file_access (file_id, apply_no, user_name, access_type, granted_name, grant_time, expire_time, status) VALUES
(2, 'YA100001', '李四', 'view', '王主任', NOW() - INTERVAL 2 DAY, NOW() + INTERVAL 28 DAY, 'active'),
(7, 'YA100005', '周八', 'view', '李处长', NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 29 DAY, 'active'),
(8, 'YA100006', '吴九', 'view', '张主任', NOW() - INTERVAL 4 DAY, NOW() + INTERVAL 26 DAY, 'active');

-- 4. 为各申请插入审计记录
-- 李四 YA100001: 受理 → 审核中 → 审批通过
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100001', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 12 DAY),
('YA100001', '审核中', '王主任', 'processing', '已收到申请，正在审核', NOW() - INTERVAL 10 DAY),
('YA100001', '审批通过', '王主任', 'approve', '经审核，该规划文件已脱密处理，同意公开。', NOW() - INTERVAL 2 DAY);

-- 王五 YA100002: 受理 → 审核中 → 驳回
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100002', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 10 DAY),
('YA100002', '审核中', '赵科长', 'processing', '需要核实文件密级', NOW() - INTERVAL 8 DAY),
('YA100002', '审批驳回', '赵科长', 'reject', '该文件涉及敏感收费信息，根据相关规定暂不予公开。', NOW() - INTERVAL 3 DAY);

-- 赵六 YA100003: 受理 → 审核中
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100003', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 5 DAY),
('YA100003', '审核中', '陈主任', 'processing', '正在核实文件内容和密级', NOW() - INTERVAL 3 DAY);

-- 孙七 YA100004: 受理
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100004', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 3 DAY);

-- 周八 YA100005: 受理 → 审核中 → 审批通过
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100005', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 8 DAY),
('YA100005', '审核中', '李处长', 'processing', '已收到申请，正在审核', NOW() - INTERVAL 6 DAY),
('YA100005', '审批通过', '李处长', 'approve', '经审核，该报告不涉及国家秘密和商业秘密，同意公开。', NOW() - INTERVAL 1 DAY);

-- 吴九 YA100006: 受理 → 审核中 → 审批通过
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100006', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 7 DAY),
('YA100006', '审核中', '张主任', 'processing', '正在审核', NOW() - INTERVAL 5 DAY),
('YA100006', '审批通过', '张主任', 'approve', '该规划经脱密审查后可予公开。', NOW() - INTERVAL 4 DAY);

-- 郑十 YA100007: 受理 → 审核中 → 驳回
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100007', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 9 DAY),
('YA100007', '审核中', '刘科长', 'processing', '需要核实采购目录密级', NOW() - INTERVAL 7 DAY),
('YA100007', '审批驳回', '刘科长', 'reject', '采购目录中涉及供应商商业秘密的部分不予公开。', NOW() - INTERVAL 6 DAY);

-- 陈十一 YA100008: 受理
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100008', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 1 DAY);

-- 李四第二次 YA100009: 受理
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100009', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 0 DAY);

-- 张三第三次 YA100010: 受理
INSERT INTO disclosure_audit_record (apply_no, node_name, operator_name, action, opinion, operate_time) VALUES
('YA100010', '受理申请', '系统', 'accept', '系统自动受理', NOW() - INTERVAL 0 DAY);

-- 验证结果
SELECT '=== 保密文件 ===' AS info;
SELECT COUNT(*) AS total_files FROM disclosure_file WHERE deleted=0;

SELECT '=== 申请记录 ===' AS info;
SELECT apply_no, applicant, id_card, status, file_id FROM disclosure_apply WHERE deleted=0 ORDER BY create_time;

SELECT '=== 访问权限 ===' AS info;
SELECT apply_no, file_id, user_name, access_type, status, expire_time FROM disclosure_file_access;

SELECT '=== 审计记录 ===' AS info;
SELECT apply_no, node_name, action, operator_name FROM disclosure_audit_record ORDER BY operate_time;
