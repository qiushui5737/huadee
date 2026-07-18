# A 模块运行与接口说明

## 功能范围

1. Elasticsearch 全文检索：统一索引知识库、CMS 内容和办事事项，支持分页、高亮、排序、热词与全量重建。
2. Spring AI Embedding 语义检索：使用向量余弦相似度召回；未配置远程向量模型时明确使用本地特征向量。
3. RAG 智能问答：真实 AI、SSE 实时输出、知识来源、多轮上下文和本地降级回答。
4. DFA 敏感词：前向和反向最大匹配；1 级替换、2 级复核、3 级拦截。
5. 会话与审计：个人会话列表、历史、重命名、删除、风险统计、详情和人工审核。

## 本地依赖

- MySQL 8，数据库名 `gov_platform`
- Redis 或 Memurai，默认 `127.0.0.1:6379`
- Elasticsearch 8.10.x，默认 `127.0.0.1:9200`
- JDK 17、Node.js 18+

先执行统一脚本：

```powershell
cmd.exe /d /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --default-character-set=utf8mb4 -uroot -p gov_platform < "sql\gov_platform_all_tables.sql"'
```

## 环境变量

聊天模型：

```text
AI_ENABLED=true
AI_BASE_URL=https://your-provider.example/v1
AI_API_KEY=your-chat-api-key
AI_CHAT_MODEL=your-chat-model-or-endpoint-id
AI_THINKING_ENABLED=false
```

独立 Embedding 模型：

```text
AI_EMBEDDING_ENABLED=true
AI_EMBEDDING_BASE_URL=https://your-provider.example/v1
AI_EMBEDDING_API_KEY=your-embedding-api-key
AI_EMBEDDING_MODEL=your-embedding-model
AI_EMBEDDING_DIMENSIONS=384
AI_EMBEDDING_MIN_SCORE=0.15
```

`AI_EMBEDDING_DIMENSIONS` 必须与模型返回维度一致；修改模型或维度后需要调用全量重建接口。

## 主要接口

| 方法 | 地址 | 说明 | 权限 |
| --- | --- | --- | --- |
| GET | `/api/v1/ai/search` | ES 关键词检索 | 公开 |
| GET | `/api/v1/ai/search/semantic` | 向量语义检索 | 公开 |
| GET | `/api/v1/ai/search/hot` | 近 30 天搜索热词 | 公开 |
| POST | `/api/v1/ai/chat/stream` | SSE 流式问答 | 可匿名 |
| GET | `/api/v1/ai/chat/conversations` | 当前用户会话列表 | 登录 |
| POST | `/api/v1/ai/chat/conversations` | 新建会话 | 登录 |
| PUT | `/api/v1/ai/chat/conversations/{sessionId}` | 重命名会话 | 登录 |
| DELETE | `/api/v1/ai/chat/conversations/{sessionId}` | 删除会话 | 登录 |
| GET | `/api/v1/ai/chat/history/{sessionId}` | 当前用户会话历史 | 登录 |
| POST | `/api/v1/ai/sensitive/check` | DFA 敏感词检测 | 公开 |
| GET/POST/PUT/DELETE | `/api/v1/ai/admin/sensitive-words` | 敏感词管理 | 管理员 |
| GET | `/api/v1/ai/admin/audits` | 问答审计分页 | 管理员 |
| GET | `/api/v1/ai/admin/audits/stats` | 审计统计 | 管理员 |
| PUT | `/api/v1/ai/admin/audits/{id}/review` | 人工审核 | 管理员 |
| GET | `/api/v1/ai/admin/search/status` | ES 与向量引擎状态 | 管理员 |
| POST | `/api/v1/ai/admin/search/rebuild` | 全量重建索引 | 管理员 |

所有普通 JSON 接口统一返回 `code`、`message`、`data`、`timestamp`。分页数据统一使用 `records`、`total`、`page`、`size`。

## 验收顺序

1. 管理端进入“AI 服务管理 -> 搜索索引”，确认 Elasticsearch 正常并执行全量重建。
2. 群众端智能搜索分别测试关键词模式和语义模式。
3. 登录群众端后连续提问两轮，刷新页面并确认历史会话可恢复。
4. 管理端新增 1、2、3 级测试敏感词，验证替换、复核、拦截策略。
5. 管理端进入问答审计，查看来源、模型、耗时并完成一条人工审核。
