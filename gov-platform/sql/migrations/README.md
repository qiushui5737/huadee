# 数据库迁移脚本说明

本目录用于保存 `gov_platform` 数据库后续变更脚本。

## 命名规则

```text
VYYYYMMDD_序号_简短说明.sql
```

示例：

```text
V20260716_01_add_service_record_handle_dept.sql
V20260716_02_add_message_priority.sql
V20260716_03_create_survey_statistics.sql
```

## 编写规则

- 一个脚本只处理一个明确变更。
- 新增表使用 `CREATE TABLE IF NOT EXISTS`。
- 新增索引尽量使用唯一名称，格式为 `idx_表名_字段名` 或 `uk_表名_字段名`。
- 新增字段要考虑已有数据，优先允许 `NULL` 或设置 `DEFAULT`。
- 不直接写 `DROP TABLE`、`TRUNCATE TABLE`、`DELETE FROM 全表`。
- 修改公共表前必须和全组确认。

## 脚本头模板

```sql
-- 变更编号：VYYYYMMDD_序号
-- 负责人：A/B/C/D/E
-- 影响模块：模块名称
-- 变更说明：一句话说明为什么改
-- 执行库：gov_platform

USE gov_platform;
```

## 执行方式

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -uroot -p050420 --default-character-set=utf8mb4 -e "source H:/huadishixixiangmu/huadee/gov-platform/sql/migrations/脚本名.sql"
```

