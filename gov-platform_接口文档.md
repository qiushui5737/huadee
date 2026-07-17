# 政府网站集约化平台 — 接口文档

> **版本**: v1.0.0  
> **更新日期**: 2026-07-15  
> **Base URL**: `http://localhost:8080`  
> **认证方式**: JWT Bearer Token（B端接口需携带 `Authorization: Bearer <token>`）

---

## 目录

| 模块 | 负责人 | 接口数 | 章节链接 |
|------|--------|--------|----------|
| A - AI智能服务 | 负责人A | 7 | [跳转](#a---ai智能服务) |
| B - 政务服务平台 | 负责人B | 6 | [跳转](#b---政务服务平台) |
| C - 互动交流平台 | 负责人C | 7 | [跳转](#c---互动交流平台) |
| D - 内容管理平台 | 负责人D | 5 | [跳转](#d---内容管理平台) |
| E - 管理与运维平台 | 负责人E | 7 | [跳转](#e---管理与运维平台) |

**共计 32 个接口**

---

## 通用说明

### 统一响应结构

所有接口均返回以下JSON结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1721030400000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码，200=成功，401=未授权，500=服务异常 |
| `message` | string | 提示信息 |
| `data` | object/array/null | 业务数据 |
| `timestamp` | long | 服务器时间戳（毫秒） |

### 分页响应结构

分页查询接口的 `data` 字段为 `PageResult` 结构：

```json
{
  "records": [],
  "total": 0,
  "current": 1,
  "size": 10,
  "pages": 0
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | array | 当前页数据列表 |
| `total` | long | 总记录数 |
| `current` | long | 当前页码 |
| `size` | long | 每页条数 |
| `pages` | long | 总页数 |

### 错误码定义

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/Token过期 |
| 403 | 禁止访问（B端需政务内网） |
| 500 | 服务器内部错误 |

### 认证机制

| 端 | Token存储 | 请求头 |
|----|-----------|--------|
| C端（群众客户端） | `localStorage.c_token` | 可选 |
| B端（政府管理端） | `localStorage.admin_token` | `Authorization: Bearer <token>` |

---

## A - AI智能服务

> 负责人A，对应 `com.gov.ai.controller`，共 7 个接口

### A1-1. 全文检索搜索

**接口说明**: 对全站政务内容进行Elasticsearch全文检索

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/ai/search` |
| **认证** | 不需要 |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | string | 是 | - | 搜索关键词 |
| `page` | int | 否 | 1 | 页码 |
| `size` | int | 否 | 10 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "title": "2026年城乡居民医保缴费标准",
        "summary": "居民医保个人缴费标准为每人每年380元...",
        "source": "卫健委",
        "url": "/cms/content/1024",
        "score": 8.92
      }
    ],
    "total": 156,
    "current": 1,
    "size": 10,
    "pages": 16
  },
  "timestamp": 1721030400000
}
```

---

### A1-2. 热门搜索词

**接口说明**: 获取当前热门搜索词列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/ai/search/hot` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": ["养老金调整", "医保报销", "居住证办理", "入学政策"],
  "timestamp": 1721030400000
}
```

---

### A3-1. 流式智能问答（SSE）

**接口说明**: 基于RAG知识库的流式问答，通过Server-Sent Events推送回答

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/ai/chat/stream` |
| **认证** | 可选（携带可关联用户会话） |
| **Content-Type** | `text/event-stream` |

**请求参数**

| 参数 | 位置 | 类型 | 必填 | 说明 |
|------|------|------|------|------|
| `question` | Query | string | 是 | 用户提问内容 |
| `Authorization` | Header | string | 否 | Bearer Token |

**响应示例（SSE流）**

```
data: {"type":"start","sessionId":"sess_001"}

data: {"type":"chunk","content":"根据"}

data: {"type":"chunk","content":"《城乡居民基本医疗保险》"}

data: {"type":"chunk","content":"政策规定..."}

data: {"type":"end","sessionId":"sess_001","sources":["卫健委-医保政策"]}
```

---

### A3-2. 获取会话历史

**接口说明**: 获取指定会话的问答历史记录（最近10轮）

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/ai/chat/history/{sessionId}` |
| **认证** | 不需要 |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `sessionId` | string | 会话ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "role": "user",
      "content": "医保报销比例是多少？",
      "timestamp": "2026-07-15T10:30:00"
    },
    {
      "role": "assistant",
      "content": "根据医保政策，城乡居民住院报销比例为70%...",
      "timestamp": "2026-07-15T10:30:02"
    }
  ],
  "timestamp": 1721030400000
}
```

---

### A4-1. 敏感词检测

**接口说明**: 使用DFA算法检测文本中的敏感词

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/ai/sensitive/check` |
| **认证** | 不需要 |

**请求体（JSON）**

```json
{
  "text": "需要检测的文本内容"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "passed": true,
    "filtered": "需要检测的文本内容",
    "matchedWords": []
  },
  "timestamp": 1721030400000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `passed` | boolean | true=通过检测，false=含敏感词 |
| `filtered` | string | 过滤后的文本（敏感词替换为*） |
| `matchedWords` | array | 命中的敏感词列表 |

---

### A4-2. 敏感词库列表

**接口说明**: 获取系统敏感词库

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/ai/sensitive/words` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": ["敏感词1", "敏感词2"],
  "timestamp": 1721030400000
}
```

---

## B - 政务服务平台

> 负责人B，对应 `com.gov.service.controller`，共 6 个接口

### B1-1. 事项分类目录

**接口说明**: 获取政务服务事项的分类目录

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/service/catalog/categories` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"code": "CAT01", "name": "户籍办理"},
    {"code": "CAT02", "name": "社保服务"},
    {"code": "CAT03", "name": "医保服务"},
    {"code": "CAT04", "name": "教育服务"},
    {"code": "CAT05", "name": "住房服务"},
    {"code": "CAT06", "name": "就业创业"}
  ],
  "timestamp": 1721030400000
}
```

---

### B1-2. 事项列表查询

**接口说明**: 按分类/关键词查询办事事项列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/service/catalog/items` |
| **认证** | 不需要 |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `category` | string | 否 | - | 分类编码（如CAT01） |
| `keyword` | string | 否 | - | 搜索关键词 |
| `page` | int | 否 | 1 | 页码 |
| `size` | int | 否 | 10 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "城乡居民医保参保登记",
        "category": "CAT03",
        "department": "医保局",
        "processingTime": "20个工作日",
        "status": "在线办理"
      }
    ],
    "total": 56,
    "current": 1,
    "size": 10,
    "pages": 6
  },
  "timestamp": 1721030400000
}
```

---

### B1-3. 事项详情

**接口说明**: 获取指定办事事项的详细信息

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/service/catalog/items/{id}` |
| **认证** | 不需要 |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 事项ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "城乡居民医保参保登记",
    "category": "CAT03",
    "department": "医保局",
    "description": "城乡居民基本医疗保险参保登记服务",
    "processingTime": "20个工作日",
    "requiredDocs": ["身份证", "户口簿"],
    "feeStandard": "不收费",
    "handleChannel": "线上/线下"
  },
  "timestamp": 1721030400000
}
```

---

### B2-1. 动态表单结构

**接口说明**: 获取指定事项的动态表单JSON Schema

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/service/form/schema/{itemId}` |
| **认证** | 不需要 |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `itemId` | long | 事项ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "itemId": 1,
    "title": "城乡居民医保参保登记表",
    "fields": [
      {"name": "applicantName", "label": "申请人姓名", "type": "text", "required": true},
      {"name": "idCard", "label": "身份证号", "type": "text", "required": true, "pattern": "^\\d{18}$"},
      {"name": "phone", "label": "联系电话", "type": "tel", "required": true},
      {"name": "address", "label": "居住地址", "type": "textarea", "required": true}
    ]
  },
  "timestamp": 1721030400000
}
```

---

### B2-2. 表单提交

**接口说明**: 提交办事申请表单，创建审批流程实例

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/service/form/submit` |
| **认证** | C端Token |

**请求体（JSON）**

```json
{
  "itemId": 1,
  "applicantName": "张三",
  "idCard": "500101199001011234",
  "phone": "13800138000",
  "address": "重庆市渝中区XX路XX号",
  "attachments": ["base64编码的文件内容"]
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "acceptNo": "SL202607151234",
    "status": "受理中"
  },
  "timestamp": 1721030400000
}
```

---

### B3-1. 审批进度查询

**接口说明**: 根据受理号查询审批流程进度

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/service/form/progress/{acceptNo}` |
| **认证** | 不需要 |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `acceptNo` | string | 受理号（如SL202607151234） |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"step": 1, "name": "窗口受理", "operator": "李四", "time": "2026-07-15 09:00", "status": "通过", "remark": "材料齐全"},
    {"step": 2, "name": "科长审核", "operator": "王五", "time": "2026-07-15 14:00", "status": "通过", "remark": "符合条件"},
    {"step": 3, "name": "领导审批", "operator": null, "time": null, "status": "待处理", "remark": null}
  ],
  "timestamp": 1721030400000
}
```

---

## C - 互动交流平台

> 负责人C，对应 `com.gov.interaction.controller`，共 7 个接口

### C1-1. 提交留言

**接口说明**: 群众向政府部门提交留言（含敏感词自动检测）

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/interaction/message` |
| **认证** | C端Token |

**请求体（JSON）**

```json
{
  "title": "关于医保报销的咨询",
  "content": "请问城乡居民医保住院报销比例是多少？",
  "category": "CAT03",
  "targetDept": "卫健委",
  "contactName": "张三",
  "contactPhone": "13800138000"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "留言提交成功",
  "data": {
    "id": 1721030400000,
    "status": "待分派"
  },
  "timestamp": 1721030400000
}
```

---

### C1-2. 留言列表查询

**接口说明**: 分页查询留言列表（C端群众可查看，B端管理员可管理）

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/interaction/message` |
| **认证** | 不需要 |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `keyword` | string | 否 | - | 搜索关键词 |
| `status` | string | 否 | - | 状态（待分派/已分派/已回复/已办结） |
| `page` | int | 否 | 1 | 页码 |
| `size` | int | 否 | 10 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1721030400000,
        "title": "关于医保报销的咨询",
        "content": "请问城乡居民医保住院报销比例是多少？",
        "category": "CAT03",
        "targetDept": "卫健委",
        "contactName": "张三",
        "status": "待分派",
        "createTime": "2026-07-15T10:00:00"
      }
    ],
    "total": 8923,
    "current": 1,
    "size": 10,
    "pages": 893
  },
  "timestamp": 1721030400000
}
```

---

### C1-3. 热门留言

**接口说明**: 获取热门留言列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/interaction/message/hot` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1721030400000,
      "title": "关于医保报销的咨询",
      "likeCount": 1289,
      "replyCount": 5,
      "dept": "卫健委"
    }
  ],
  "timestamp": 1721030400000
}
```

---

### C2-1. 留言回复（B端）

**接口说明**: B端政府管理人员回复群众留言

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/interaction/message/{id}/reply` |
| **认证** | B端Token（必需） |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 留言ID |

**请求体（JSON）**

```json
{
  "content": "您好，城乡居民医保住院报销比例为70%，具体可咨询12333。",
  "operator": "卫健委-李四"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1721030400000
}
```

---

### C3-1. 依申请公开—提交申请

**接口说明**: 群众向政府提交信息公开申请

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/disclosure/apply` |
| **认证** | C端Token |

**请求体（JSON）**

```json
{
  "applicantName": "张三",
  "idCard": "500101199001011234",
  "phone": "13800138000",
  "address": "重庆市渝中区XX路XX号",
  "targetDept": "卫健委",
  "content": "申请公开2026年医保基金使用情况",
  "purpose": "了解医保基金运行状况",
  "deliveryMethod": "电子邮件"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "申请提交成功",
  "data": {
    "applyNo": "YA202612345",
    "status": "已受理"
  },
  "timestamp": 1721030400000
}
```

---

### C3-2. 依申请公开—进度查询

**接口说明**: 根据申请编号查询依申请公开办理进度

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/disclosure/progress/{applyNo}` |
| **认证** | 不需要 |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `applyNo` | string | 申请编号（如YA202612345） |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"step": 1, "name": "申请受理", "time": "2026-07-15 09:00", "status": "完成"},
    {"step": 2, "name": "内容审查", "time": "2026-07-16 10:00", "status": "进行中"},
    {"step": 3, "name": "领导审批", "time": null, "status": "待处理"},
    {"step": 4, "name": "结果送达", "time": null, "status": "待处理"}
  ],
  "timestamp": 1721030400000
}
```

---

### C4-1. 依申请公开—审核（B端）

**接口说明**: B端审核依申请公开申请

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/disclosure/audit/{applyNo}` |
| **认证** | B端Token（必需） |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `applyNo` | string | 申请编号 |

**请求体（JSON）**

```json
{
  "action": "approve",
  "remark": "符合公开条件，予以公开",
  "operator": "卫健委-王五"
}
```

| action值 | 说明 |
|----------|------|
| `approve` | 通过（同意公开） |
| `reject` | 驳回（不予公开） |
| `supplement` | 要求补正材料 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1721030400000
}
```

---

## D - 内容管理平台

> 负责人D，对应 `com.gov.cms.controller`，共 5 个接口

### D1-1. 部门子站列表

**接口说明**: 获取所有部门子站列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/cms/sites` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"code": "EDU", "name": "教育部"},
    {"code": "HEA", "name": "卫健委"},
    {"code": "HOU", "name": "住建局"},
    {"code": "SOC", "name": "人社局"}
  ],
  "timestamp": 1721030400000
}
```

---

### D2-1. 内容列表查询

**接口说明**: 分页查询CMS内容列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/cms/content/list` |
| **认证** | 不需要 |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `siteCode` | string | 否 | - | 部门子站编码（如EDU） |
| `category` | string | 否 | - | 内容分类（通知/公告/政策/新闻） |
| `page` | int | 否 | 1 | 页码 |
| `size` | int | 否 | 10 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1024,
        "title": "2026年城乡居民医保缴费标准",
        "category": "政策",
        "siteCode": "HEA",
        "siteName": "卫健委",
        "author": "卫健委",
        "publishTime": "2026-07-10",
        "status": "已发布",
        "viewCount": 12890
      }
    ],
    "total": 234,
    "current": 1,
    "size": 10,
    "pages": 24
  },
  "timestamp": 1721030400000
}
```

---

### D2-2. 内容发布（B端）

**接口说明**: B端编辑发布内容，提交后自动进入审核流程

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/cms/content` |
| **认证** | B端Token（必需） |

**请求体（JSON）**

```json
{
  "title": "2026年城乡居民医保缴费标准",
  "content": "<p>居民医保个人缴费标准为每人每年380元...</p>",
  "category": "政策",
  "siteCode": "HEA",
  "author": "卫健委",
  "attachments": ["文件URL"]
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "内容已提交审核",
  "data": {
    "contentId": 1721030400000,
    "status": "待审核"
  },
  "timestamp": 1721030400000
}
```

---

### D3-1. 内容审核（B端）

**接口说明**: B端内容审核（初审→复审→终审三级审核）

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/cms/audit/{contentId}` |
| **认证** | B端Token（必需） |

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `contentId` | long | 内容ID |

**请求体（JSON）**

```json
{
  "action": "approve",
  "stage": "初审",
  "remark": "内容合规，初审通过",
  "operator": "编辑部-赵六"
}
```

| action值 | 说明 |
|----------|------|
| `approve` | 审核通过 |
| `reject` | 审核驳回（退回修改） |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1721030400000
}
```

---

### D4-1. 信息公开目录

**接口说明**: 获取信息公开目录树结构

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/cms/disclosure/catalog` |
| **认证** | 不需要 |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "DC01",
      "name": "机构职能",
      "children": [
        {"code": "DC0101", "name": "机构概况"},
        {"code": "DC0102", "name": "领导信息"}
      ]
    },
    {
      "code": "DC02",
      "name": "政策文件",
      "children": [
        {"code": "DC0201", "name": "法律法规"},
        {"code": "DC0202", "name": "部门文件"}
      ]
    }
  ],
  "timestamp": 1721030400000
}
```

---

## E - 管理与运维平台

> 负责人E，对应 `com.gov.admin.controller`，共 7 个接口

### E1-1. 大屏仪表盘数据

**接口说明**: 获取管理端大屏所需的核心指标和趋势数据

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/admin/stats/dashboard` |
| **认证** | B端Token（必需） |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalVisits": 1284567,
    "totalServices": 35678,
    "totalMessages": 8923,
    "satisfactionRate": 96.8,
    "weeklyTrend": [
      {"date": "周一", "visits": 18234, "services": 4567},
      {"date": "周二", "visits": 19567, "services": 5023},
      {"date": "周三", "visits": 20890, "services": 5456},
      {"date": "周四", "visits": 22345, "services": 5789},
      {"date": "周五", "visits": 23678, "services": 6012},
      {"date": "周六", "visits": 12890, "services": 3234},
      {"date": "周日", "visits": 10987, "services": 2890}
    ]
  },
  "timestamp": 1721030400000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `totalVisits` | int | 总访问量 |
| `totalServices` | int | 总办件量 |
| `totalMessages` | int | 总留言数 |
| `satisfactionRate` | double | 满意率（%） |
| `weeklyTrend` | array | 近7天趋势数据 |

---

### E1-2. 统计报表

**接口说明**: 按日期范围查询统计报表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/admin/stats/report` |
| **认证** | B端Token（必需） |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `startDate` | string | 否 | - | 起始日期（yyyy-MM-dd） |
| `endDate` | string | 否 | - | 结束日期（yyyy-MM-dd） |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "visits": {"total": 128456, "dailyAvg": 42819},
    "services": {"total": 35678, "completed": 32145, "processing": 3533},
    "messages": {"total": 8923, "replied": 7890, "pending": 1033},
    "departments": [
      {"dept": "教育部", "visits": 23456, "services": 5678},
      {"dept": "卫健委", "visits": 34567, "services": 8901}
    ]
  },
  "timestamp": 1721030400000
}
```

---

### E3-1. 部门绩效排名

**接口说明**: 获取各部门绩效排名

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/admin/performance/ranking` |
| **认证** | B端Token（必需） |

**请求参数**: 无

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"dept": "教育部", "score": 92.5, "rank": 1},
    {"dept": "卫健委", "score": 89.3, "rank": 2},
    {"dept": "人社局", "score": 87.1, "rank": 3}
  ],
  "timestamp": 1721030400000
}
```

---

### E4-1. 管理员登录

**接口说明**: B端管理员登录（需双因素认证+政务内网/VPN环境）

| 项目 | 值 |
|------|-----|
| **URL** | `POST /api/v1/admin/auth/login` |
| **认证** | 不需要（登录接口） |

**请求体（JSON）**

```json
{
  "username": "admin",
  "password": "123456",
  "smsCode": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 工号/用户名 |
| `password` | string | 是 | 密码 |
| `smsCode` | string | 是 | 短信验证码（双因素认证） |

**响应示例**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIs...",
    "username": "admin",
    "role": "系统管理员"
  },
  "timestamp": 1721030400000
}
```

---

### E4-2. 获取当前用户信息

**接口说明**: 根据Token获取当前登录管理员的用户信息和权限

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/admin/auth/info` |
| **认证** | B端Token（必需） |

**请求头**

| Header | 说明 |
|--------|------|
| `Authorization` | `Bearer <token>` |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "username": "管理员",
    "roles": ["ADMIN"],
    "permissions": ["*:*"]
  },
  "timestamp": 1721030400000
}
```

---

### E5-1. 消息列表查询

**接口说明**: 查询站内消息/系统通知列表

| 项目 | 值 |
|------|-----|
| **URL** | `GET /api/v1/admin/message/list` |
| **认证** | B端Token（必需） |

**请求参数（Query）**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `type` | string | 否 | - | 消息类型（系统通知/待办提醒/预警消息） |
| `page` | int | 否 | 1 | 页码 |
| `size` | int | 否 | 10 | 每页条数 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "您有3条待审核留言",
        "content": "教育部有3条新留言待处理...",
        "type": "待办提醒",
        "isRead": false,
        "createTime": "2026-07-15T08:30:00"
      }
    ],
    "total": 15,
    "current": 1,
    "size": 10,
    "pages": 2
  },
  "timestamp": 1721030400000
}
```

---

## 附录

### 接口速查表

| 编号 | 方法 | URL | 认证 | 说明 |
|------|------|-----|------|------|
| A1-1 | GET | `/api/v1/ai/search` | - | 全文检索 |
| A1-2 | GET | `/api/v1/ai/search/hot` | - | 热门搜索词 |
| A3-1 | GET | `/api/v1/ai/chat/stream` | 可选 | 流式问答(SSE) |
| A3-2 | GET | `/api/v1/ai/chat/history/{sessionId}` | - | 会话历史 |
| A4-1 | POST | `/api/v1/ai/sensitive/check` | - | 敏感词检测 |
| A4-2 | GET | `/api/v1/ai/sensitive/words` | - | 敏感词库 |
| B1-1 | GET | `/api/v1/service/catalog/categories` | - | 事项分类目录 |
| B1-2 | GET | `/api/v1/service/catalog/items` | - | 事项列表查询 |
| B1-3 | GET | `/api/v1/service/catalog/items/{id}` | - | 事项详情 |
| B2-1 | GET | `/api/v1/service/form/schema/{itemId}` | - | 动态表单结构 |
| B2-2 | POST | `/api/v1/service/form/submit` | C端 | 表单提交 |
| B3-1 | GET | `/api/v1/service/form/progress/{acceptNo}` | - | 审批进度查询 |
| C1-1 | POST | `/api/v1/interaction/message` | C端 | 提交留言 |
| C1-2 | GET | `/api/v1/interaction/message` | - | 留言列表 |
| C1-3 | GET | `/api/v1/interaction/message/hot` | - | 热门留言 |
| C2-1 | POST | `/api/v1/interaction/message/{id}/reply` | B端 | 留言回复 |
| C3-1 | POST | `/api/v1/disclosure/apply` | C端 | 依申请公开-提交 |
| C3-2 | GET | `/api/v1/disclosure/progress/{applyNo}` | - | 依申请公开-进度 |
| C4-1 | POST | `/api/v1/disclosure/audit/{applyNo}` | B端 | 依申请公开-审核 |
| D1-1 | GET | `/api/v1/cms/sites` | - | 部门子站列表 |
| D2-1 | GET | `/api/v1/cms/content/list` | - | 内容列表 |
| D2-2 | POST | `/api/v1/cms/content` | B端 | 内容发布 |
| D3-1 | POST | `/api/v1/cms/audit/{contentId}` | B端 | 内容审核 |
| D4-1 | GET | `/api/v1/cms/disclosure/catalog` | - | 信息公开目录 |
| E1-1 | GET | `/api/v1/admin/stats/dashboard` | B端 | 大屏仪表盘 |
| E1-2 | GET | `/api/v1/admin/stats/report` | B端 | 统计报表 |
| E3-1 | GET | `/api/v1/admin/performance/ranking` | B端 | 绩效排名 |
| E4-1 | POST | `/api/v1/admin/auth/login` | - | 管理员登录 |
| E4-2 | GET | `/api/v1/admin/auth/info` | B端 | 用户信息 |
| E5-1 | GET | `/api/v1/admin/message/list` | B端 | 消息列表 |

### 分支与负责人映射

| 分支 | 负责人 | 模块范围 | 对应包路径 |
|------|--------|----------|------------|
| `dev-ai` | 负责人A | A1-A5 (AI智能服务) | `com.gov.ai` |
| `dev-service` | 负责人B | B1-B5 (政务服务平台) | `com.gov.service` |
| `dev-interaction` | 负责人C | C1-C5 (互动交流平台) | `com.gov.interaction` |
| `dev-cms` | 负责人D | D1-D5 (内容管理平台) | `com.gov.cms` |
| `dev-admin` | 负责人E | E1-E5 (管理与运维平台) | `com.gov.admin` |
| `main` | 项目组 | 集成主分支 | 全部 |
