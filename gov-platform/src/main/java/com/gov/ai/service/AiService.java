package com.gov.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.common.result.PageResult;
import com.gov.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class AiService {
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final ElasticsearchSearchService elasticsearchSearchService;
    private final SensitiveWordService sensitiveWordService;

    @Value("${ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${ai.base-url:}")
    private String aiBaseUrl;

    @Value("${ai.api-key:}")
    private String aiApiKey;

    @Value("${ai.chat-model:gpt-4o-mini}")
    private String aiChatModel;

    @Value("${ai.timeout-seconds:30}")
    private int aiTimeoutSeconds;

    @Value("${ai.thinking-enabled:false}")
    private boolean aiThinkingEnabled;

    @Value("${ai.embedding.min-score:0.15}")
    private double semanticMinScore;

    public AiService(DataSource dataSource, ObjectMapper objectMapper,
            ElasticsearchSearchService elasticsearchSearchService,
            SensitiveWordService sensitiveWordService) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
        this.elasticsearchSearchService = elasticsearchSearchService;
        this.sensitiveWordService = sensitiveWordService;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public PageResult<Map<String, Object>> search(String keyword, int page, int size) {
        String term = clean(keyword);
        if (term.isBlank()) throw BusinessException.of(400, "搜索关键词不能为空");
        long started = System.currentTimeMillis();
        if (elasticsearchSearchService.isAvailable()) {
            try {
                PageResult<Map<String, Object>> result = elasticsearchSearchService.search(term, page, size);
                elasticsearchSearchService.saveSearchLog(term, "keyword", result.getTotal(), "elasticsearch", System.currentTimeMillis() - started);
                return result;
            } catch (Exception ignored) {
                // Fall through to the database search so the public service stays available.
            }
        }
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(searchKnowledge(term));
        all.addAll(searchCms(term));
        all.addAll(searchServices(term));
        all.sort(Comparator.comparingDouble(item -> -toDouble(item.get("score"))));
        PageResult<Map<String, Object>> result = page(all, page, size);
        elasticsearchSearchService.saveSearchLog(term, "keyword", result.getTotal(), "mysql-fallback", System.currentTimeMillis() - started);
        return result;
    }

    public PageResult<Map<String, Object>> semanticSearch(String keyword, int page, int size) {
        String term = clean(keyword);
        if (term.isBlank()) throw BusinessException.of(400, "语义搜索内容不能为空");
        long started = System.currentTimeMillis();
        if (elasticsearchSearchService.isAvailable()) {
            try {
                PageResult<Map<String, Object>> result = elasticsearchSearchService.semanticSearch(
                        term, page, size, semanticMinScore);
                String engine = result.getRecords().isEmpty() ? "vector" : String.valueOf(result.getRecords().get(0).get("engine"));
                elasticsearchSearchService.saveSearchLog(term, "semantic", result.getTotal(), engine,
                        System.currentTimeMillis() - started);
                return result;
            } catch (Exception ignored) {
                // Keep the public endpoint available if the vector index is being rebuilt.
            }
        }
        PageResult<Map<String, Object>> fallback = search(term, page, size);
        fallback.getRecords().forEach(item -> {
            item.put("matchMode", "keyword-fallback");
            item.put("engine", "keyword-fallback");
        });
        return fallback;
    }

    public List<String> hotKeywords() {
        List<String> words = elasticsearchSearchService.hotKeywords();
        return words.isEmpty()
                ? List.of("学生资助", "健康证明", "政府信息公开", "社保参保证明", "办事进度")
                : words;
    }

    public Map<String, Object> checkSensitive(String text) {
        return sensitiveWordService.check(text);
    }

    public List<Map<String, Object>> sensitiveWords() {
        return sensitiveWordService.words();
    }

    public Map<String, Object> answer(String question, String sessionId) {
        return answer(question, sessionId, null);
    }

    public Map<String, Object> answer(String question, String sessionId, Long userId) {
        validateQuestion(question);
        long started = System.currentTimeMillis();
        String safeSession = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : sessionId;
        ensureConversation(safeSession, userId, question);
        List<Map<String, String>> context = loadRecentContext(safeSession, userId, 6);
        Map<String, Object> sensitive = checkSensitive(question);
        List<Map<String, Object>> refs = semanticSearch(question, 1, 4).getRecords();
        boolean passed = Boolean.TRUE.equals(sensitive.get("passed"));
        String answer = passed ? buildAnswer(question, refs) : sensitiveTip();
        String modelName = "local-rag-demo";

        if (passed && realAiConfigured()) {
            try {
                answer = callOpenAiCompatibleChat(question, refs, context);
                modelName = aiChatModel;
            } catch (Exception e) {
                answer = answer + "\n\n提示：真实 AI 接口调用失败，已自动使用本地知识库回答。失败原因：" + e.getMessage();
            }
        }

        saveChat(safeSession, userId, question, answer, refs, modelName, passed ? "normal" : "sensitive",
                ((Number) sensitive.get("maxLevel")).intValue(), System.currentTimeMillis() - started);
        touchConversation(safeSession);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", safeSession);
        data.put("answer", answer);
        data.put("sources", refs);
        data.put("sensitive", sensitive);
        data.put("modelName", modelName);
        data.put("aiEnabled", realAiConfigured());
        data.put("time", LocalDateTime.now().toString());
        return data;
    }

    public Map<String, Object> streamAnswer(String question, String sessionId,
            Long userId,
            Consumer<Map<String, Object>> metaConsumer,
            Consumer<String> chunkConsumer) {
        validateQuestion(question);
        long started = System.currentTimeMillis();
        String safeSession = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : sessionId;
        ensureConversation(safeSession, userId, question);
        List<Map<String, String>> context = loadRecentContext(safeSession, userId, 6);
        Map<String, Object> sensitive = checkSensitive(question);
        List<Map<String, Object>> refs = semanticSearch(question, 1, 4).getRecords();
        boolean passed = Boolean.TRUE.equals(sensitive.get("passed"));

        metaConsumer.accept(Map.of(
            "sessionId", safeSession,
            "sources", refs,
            "sensitive", sensitive,
            "aiEnabled", realAiConfigured(),
            "modelName", realAiConfigured() ? aiChatModel : "local-rag-demo"
        ));

        String answer;
        String modelName = "local-rag-demo";
        if (!passed) {
            answer = sensitiveTip();
            emitFallbackChunks(answer, chunkConsumer);
        } else if (realAiConfigured()) {
            try {
                answer = callOpenAiCompatibleChatStream(question, refs, context, chunkConsumer);
                modelName = aiChatModel;
            } catch (Exception e) {
                String fallback = buildAnswer(question, refs)
                    + "\n\n提示：真实 AI 流式接口调用失败，已自动使用本地知识库回答。失败原因：" + e.getMessage();
                answer = fallback;
                emitFallbackChunks(fallback, chunkConsumer);
            }
        } else {
            answer = buildAnswer(question, refs);
            emitFallbackChunks(answer, chunkConsumer);
        }

        saveChat(safeSession, userId, question, answer, refs, modelName, passed ? "normal" : "sensitive",
                ((Number) sensitive.get("maxLevel")).intValue(), System.currentTimeMillis() - started);
        touchConversation(safeSession);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", safeSession);
        data.put("answer", answer);
        data.put("sources", refs);
        data.put("sensitive", sensitive);
        data.put("modelName", modelName);
        data.put("aiEnabled", realAiConfigured());
        data.put("time", LocalDateTime.now().toString());
        return data;
    }

    public List<Map<String, Object>> history(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw BusinessException.of(400, "会话ID不能为空");
        }
        String sql = "select id, session_id, question, answer, sources, audit_status, create_time from chat_session where session_id = ? and user_id = ? order by id asc limit 50";
        return query(sql, ps -> {
            ps.setString(1, sessionId);
            ps.setLong(2, userId);
        }, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("sessionId", rs.getString("session_id"));
            row.put("question", rs.getString("question"));
            row.put("answer", rs.getString("answer"));
            row.put("sources", rs.getString("sources"));
            row.put("auditStatus", rs.getString("audit_status"));
            row.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
            return row;
        });
    }

    public List<Map<String, Object>> conversations(Long userId) {
        String sql = "select session_id,title,memory_summary,last_message_time,create_time "
                + "from ai_conversation where user_id=? and deleted=0 "
                + "order by coalesce(last_message_time,create_time) desc limit 100";
        return query(sql, ps -> ps.setLong(1, userId), rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("sessionId", rs.getString("session_id"));
            row.put("title", rs.getString("title"));
            row.put("memorySummary", rs.getString("memory_summary"));
            row.put("lastMessageTime", String.valueOf(rs.getTimestamp("last_message_time")));
            row.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
            return row;
        });
    }

    public Map<String, Object> createConversation(Long userId, String title) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String safeTitle = clean(title).isBlank() ? "新会话" : truncate(clean(title), 60);
        update("insert into ai_conversation(session_id,user_id,title,last_message_time,deleted) values(?,?,?,now(),0)", ps -> {
            ps.setString(1, sessionId);
            ps.setLong(2, userId);
            ps.setString(3, safeTitle);
        });
        return Map.of("sessionId", sessionId, "title", safeTitle);
    }

    public void renameConversation(String sessionId, Long userId, String title) {
        String safeTitle = truncate(clean(title), 60);
        if (safeTitle.isBlank()) throw BusinessException.of(400, "会话标题不能为空");
        int changed = updateCount("update ai_conversation set title=?,update_time=now() "
                + "where session_id=? and user_id=? and deleted=0", ps -> {
            ps.setString(1, safeTitle);
            ps.setString(2, sessionId);
            ps.setLong(3, userId);
        });
        if (changed == 0) throw BusinessException.of(404, "会话不存在");
    }

    public void deleteConversation(String sessionId, Long userId) {
        int changed = updateCount("update ai_conversation set deleted=1,update_time=now() "
                + "where session_id=? and user_id=? and deleted=0", ps -> {
            ps.setString(1, sessionId);
            ps.setLong(2, userId);
        });
        if (changed == 0) throw BusinessException.of(404, "会话不存在");
    }

    public PageResult<Map<String, Object>> auditList(String keyword, String status, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        StringBuilder sql = new StringBuilder("select c.id,c.session_id,c.user_id,u.username,c.question,c.answer,c.sources,c.model_name,"
                + "c.audit_status,c.risk_level,c.review_status,c.review_by,c.review_comment,c.review_time,c.response_ms,c.create_time "
                + "from chat_session c left join sys_user u on u.id=c.user_id where 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (c.question like ? or c.answer like ? or c.session_id like ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and (c.audit_status = ? or c.review_status = ?)");
            params.add(status);
            params.add(status);
        }
        sql.append(" order by c.id desc limit ? offset ?");
        params.add(safeSize);
        params.add((safePage - 1) * safeSize);

        List<Map<String, Object>> records = query(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("sessionId", rs.getString("session_id"));
            row.put("userId", rs.getObject("user_id"));
            row.put("username", rs.getString("username"));
            row.put("question", rs.getString("question"));
            row.put("answer", rs.getString("answer"));
            row.put("sources", rs.getString("sources"));
            row.put("modelName", rs.getString("model_name"));
            row.put("auditStatus", rs.getString("audit_status"));
            row.put("riskLevel", rs.getInt("risk_level"));
            row.put("reviewStatus", rs.getString("review_status"));
            row.put("reviewBy", rs.getObject("review_by"));
            row.put("reviewComment", rs.getString("review_comment"));
            row.put("reviewTime", String.valueOf(rs.getTimestamp("review_time")));
            row.put("responseMs", rs.getInt("response_ms"));
            row.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
            return row;
        });
        return PageResult.of(records, countAudit(keyword, status), safePage, safeSize);
    }

    public Map<String, Object> auditDetail(long id) {
        List<Map<String, Object>> rows = auditRowsById(id);
        if (rows.isEmpty()) throw BusinessException.of(404, "审计记录不存在");
        return rows.get(0);
    }

    public Map<String, Object> auditStats() {
        String sql = "select count(*) total,sum(create_time>=curdate()) today_count,"
                + "sum(audit_status='sensitive') sensitive_count,sum(review_status='pending') pending_count,"
                + "round(avg(response_ms)) avg_response_ms from chat_session";
        List<Map<String, Object>> rows = query(sql, ps -> {}, rs -> Map.of(
                "total", rs.getLong("total"),
                "today", rs.getLong("today_count"),
                "sensitive", rs.getLong("sensitive_count"),
                "pending", rs.getLong("pending_count"),
                "avgResponseMs", rs.getLong("avg_response_ms")
        ));
        return rows.isEmpty() ? Map.of("total", 0, "today", 0, "sensitive", 0, "pending", 0, "avgResponseMs", 0) : rows.get(0);
    }

    public void reviewAudit(long id, Long reviewerId, String status, String comment) {
        String reviewStatus = clean(status);
        if (!List.of("approved", "rejected").contains(reviewStatus)) {
            throw BusinessException.of(400, "审核状态只能是 approved 或 rejected");
        }
        int changed = updateCount("update chat_session set review_status=?,review_by=?,review_comment=?,review_time=now() where id=?", ps -> {
            ps.setString(1, reviewStatus);
            ps.setLong(2, reviewerId);
            ps.setString(3, truncate(clean(comment), 500));
            ps.setLong(4, id);
        });
        if (changed == 0) throw BusinessException.of(404, "审计记录不存在");
    }

    private List<Map<String, Object>> auditRowsById(long id) {
        String sql = "select c.id,c.session_id,c.user_id,u.username,c.question,c.answer,c.sources,c.model_name,"
                + "c.audit_status,c.risk_level,c.review_status,c.review_by,c.review_comment,c.review_time,c.response_ms,c.create_time "
                + "from chat_session c left join sys_user u on u.id=c.user_id where c.id=?";
        return query(sql, ps -> ps.setLong(1, id), rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id")); row.put("sessionId", rs.getString("session_id"));
            row.put("userId", rs.getObject("user_id")); row.put("username", rs.getString("username"));
            row.put("question", rs.getString("question")); row.put("answer", rs.getString("answer"));
            row.put("sources", rs.getString("sources")); row.put("modelName", rs.getString("model_name"));
            row.put("auditStatus", rs.getString("audit_status")); row.put("riskLevel", rs.getInt("risk_level"));
            row.put("reviewStatus", rs.getString("review_status")); row.put("reviewBy", rs.getObject("review_by"));
            row.put("reviewComment", rs.getString("review_comment")); row.put("reviewTime", String.valueOf(rs.getTimestamp("review_time")));
            row.put("responseMs", rs.getInt("response_ms")); row.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
            return row;
        });
    }

    private boolean realAiConfigured() {
        return aiEnabled && !clean(aiBaseUrl).isBlank() && !clean(aiApiKey).isBlank();
    }

    private String callOpenAiCompatibleChat(String question, List<Map<String, Object>> refs,
            List<Map<String, String>> context) throws Exception {
        String endpoint = clean(aiBaseUrl);
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        if (!endpoint.endsWith("/chat/completions")) {
            endpoint = endpoint + "/chat/completions";
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiChatModel);
        payload.put("temperature", 0.2);
        payload.put("stream", false);
        payload.put("thinking", Map.of("type", aiThinkingEnabled ? "enabled" : "disabled"));
        payload.put("messages", buildAiMessages(question, refs, context));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofSeconds(Math.max(5, aiTimeoutSeconds)))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + aiApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (content.isMissingNode() || content.asText().isBlank()) {
            throw new IllegalStateException("AI response has no message content");
        }
        return content.asText();
    }

    private String callOpenAiCompatibleChatStream(String question, List<Map<String, Object>> refs,
            List<Map<String, String>> context, Consumer<String> chunkConsumer) throws Exception {
        String endpoint = chatCompletionEndpoint();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiChatModel);
        payload.put("temperature", 0.2);
        payload.put("stream", true);
        payload.put("thinking", Map.of("type", aiThinkingEnabled ? "enabled" : "disabled"));
        payload.put("messages", buildAiMessages(question, refs, context));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofSeconds(Math.max(5, aiTimeoutSeconds)))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + aiApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + errorBody);
        }

        StringBuilder answer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("data:")) {
                    continue;
                }
                String data = trimmed.substring("data:".length()).trim();
                if ("[DONE]".equals(data)) {
                    break;
                }
                if (data.isBlank()) {
                    continue;
                }
                JsonNode root = objectMapper.readTree(data);
                JsonNode delta = root.path("choices").path(0).path("delta").path("content");
                if (!delta.isMissingNode() && !delta.asText().isEmpty()) {
                    String chunk = delta.asText();
                    answer.append(chunk);
                    chunkConsumer.accept(chunk);
                }
            }
        }
        if (answer.isEmpty()) {
            throw new IllegalStateException("AI stream response has no content");
        }
        return answer.toString();
    }

    private String chatCompletionEndpoint() {
        String endpoint = clean(aiBaseUrl);
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        if (!endpoint.endsWith("/chat/completions")) {
            endpoint = endpoint + "/chat/completions";
        }
        return endpoint;
    }

    private void emitFallbackChunks(String answer, Consumer<String> chunkConsumer) {
        for (String chunk : answer.split("(?<=。|！|？|；|\\n)")) {
            if (!chunk.isBlank()) {
                chunkConsumer.accept(chunk);
            }
        }
    }

    private String systemPrompt() {
        return """
            你是政府网站集约化平台的政务智能助手。
            回答要求：
            1. 使用中文，语气正式、清晰、友好。
            2. 优先参考提供的知识库资料回答。
            3. 知识库资料不足时，可以基于通用政务知识进行回答。
            4. 不能编造具体政策编号、办理时限、收费标准、咨询电话和办理地点。
            5. 如果涉及材料、流程、注意事项，尽量用条目或表格说明。
            6. 对无法确认的具体事项，要提示用户以当地政府网站、政务服务平台或主管部门答复为准。
            7. 不输出与政务咨询无关的内容。
            """;
    }

    private String buildRagPrompt(String question, List<Map<String, Object>> refs) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户问题：").append(question).append("\n\n");
        sb.append("检索到的资料：\n");
        if (refs.isEmpty()) {
            sb.append("暂无匹配资料。\n");
        } else {
            for (int i = 0; i < refs.size(); i++) {
                Map<String, Object> ref = refs.get(i);
                sb.append(i + 1).append(". 【").append(ref.get("type")).append("】")
                    .append(ref.get("title")).append("\n")
                    .append("摘要：").append(ref.get("summary")).append("\n")
                    .append("来源：").append(ref.get("source")).append("\n\n");
            }
        }
        sb.append("请结合以上资料回答用户问题。");
        return sb.toString();
    }

    private List<Map<String, Object>> buildAiMessages(String question, List<Map<String, Object>> refs,
            List<Map<String, String>> context) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt()));
        for (Map<String, String> turn : context) {
            messages.add(Map.of("role", "user", "content", turn.getOrDefault("question", "")));
            messages.add(Map.of("role", "assistant", "content", turn.getOrDefault("answer", "")));
        }
        messages.add(Map.of("role", "user", "content", buildRagPrompt(question, refs)));
        return messages;
    }

    private void ensureConversation(String sessionId, Long userId, String firstQuestion) {
        String sql = "select user_id from ai_conversation where session_id=? limit 1";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long ownerId = rs.getObject("user_id") == null ? null : rs.getLong("user_id");
                    if (ownerId != null && !ownerId.equals(userId)) {
                        throw BusinessException.of(403, "无权访问该会话");
                    }
                    if (ownerId == null && userId != null) {
                        update("update ai_conversation set user_id=?,deleted=0 where session_id=?", update -> {
                            update.setLong(1, userId);
                            update.setString(2, sessionId);
                        });
                    }
                    return;
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("读取会话失败: " + e.getMessage(), e);
        }
        String title = truncate(clean(firstQuestion), 30);
        update("insert into ai_conversation(session_id,user_id,title,last_message_time,deleted) values(?,?,?,now(),0)", ps -> {
            ps.setString(1, sessionId);
            if (userId == null) ps.setNull(2, java.sql.Types.BIGINT); else ps.setLong(2, userId);
            ps.setString(3, title.isBlank() ? "新会话" : title);
        });
    }

    private List<Map<String, String>> loadRecentContext(String sessionId, Long userId, int turns) {
        List<Map<String, String>> rows = query(
                "select question,answer from chat_session where session_id=? order by id desc limit ?",
                ps -> {
                    ps.setString(1, sessionId);
                    ps.setInt(2, Math.max(1, Math.min(20, turns)));
                },
                rs -> Map.of("question", clean(rs.getString("question")), "answer", clean(rs.getString("answer"))));
        Collections.reverse(rows);
        return rows;
    }

    private void touchConversation(String sessionId) {
        update("update ai_conversation set last_message_time=now(),deleted=0 where session_id=?",
                ps -> ps.setString(1, sessionId));
    }

    private List<Map<String, Object>> searchKnowledge(String term) {
        String sql = "select id, title, content, source, dept_code, doc_type, create_time from knowledge_doc where deleted = 0 and status = 1 and (title like ? or content like ? or dept_code like ?) limit 30";
        String like = "%" + term + "%";
        return query(sql, ps -> {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
        }, rs -> resultRow(rs.getLong("id"), rs.getString("title"), rs.getString("content"), "知识库", rs.getString("source"), rs.getString("dept_code"), "/chat", term, rs.getTimestamp("create_time")));
    }

    private List<Map<String, Object>> searchCms(String term) {
        String sql = "select id, title, content, category, site_code, create_time from cms_content where deleted = 0 and status in ('published', 'approved') and (title like ? or content like ? or category like ?) limit 30";
        String like = "%" + term + "%";
        return query(sql, ps -> {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
        }, rs -> resultRow(rs.getLong("id"), rs.getString("title"), rs.getString("content"), "信息公开", rs.getString("category"), rs.getString("site_code"), "/dept", term, rs.getTimestamp("create_time")));
    }

    private List<Map<String, Object>> searchServices(String term) {
        String sql = "select id, item_name, description, category, dept_code, create_time from service_item where deleted = 0 and status = 1 and (item_name like ? or description like ? or category like ? or dept_code like ?) limit 30";
        String like = "%" + term + "%";
        return query(sql, ps -> {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
        }, rs -> resultRow(rs.getLong("id"), rs.getString("item_name"), rs.getString("description"), "办事服务", rs.getString("category"), rs.getString("dept_code"), "/service", term, rs.getTimestamp("create_time")));
    }

    private Map<String, Object> resultRow(long id, String title, String content, String type, String source, String deptCode, String url, String term, Object createTime) {
        String summary = summarize(content);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("title", title);
        row.put("summary", summary);
        row.put("highlight", highlight(summary, term));
        row.put("type", type);
        row.put("source", source);
        row.put("deptCode", deptCode);
        row.put("url", url);
        row.put("score", score(term, title, content, type));
        row.put("matchMode", "keyword");
        row.put("createTime", String.valueOf(createTime));
        return row;
    }

    private String buildAnswer(String question, List<Map<String, Object>> refs) {
        if (refs.isEmpty()) {
            return "我暂时没有在本平台知识库中找到完全匹配的信息。建议您换一个关键词，或进入办事服务、信息公开栏目继续查询。";
        }
        Map<String, Object> top = refs.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("根据平台知识库，关于“").append(question).append("”，可优先参考《")
            .append(top.get("title")).append("》。");
        sb.append("该内容属于").append(top.get("type")).append("，主要说明：").append(top.get("summary"));
        if (refs.size() > 1) {
            sb.append(" 同时还检索到 ").append(refs.size() - 1).append(" 条相关资料，可在下方来源中继续查看。");
        }
        return sb.toString();
    }

    private String sensitiveTip() {
        return "您的问题中包含需要人工复核的敏感内容。请调整表述后重新咨询，或通过政民互动渠道提交正式诉求。";
    }

    private void saveChat(String sessionId, Long userId, String question, String answer,
            List<Map<String, Object>> refs, String modelName, String auditStatus, int riskLevel, long responseMs) {
        String sql = "insert into chat_session(session_id,user_id,question,answer,sources,model_name,audit_status,risk_level,review_status,response_ms) "
                + "values(?,?,?,?,?,?,?,?,?,?)";
        update(sql, ps -> {
            ps.setString(1, sessionId);
            if (userId == null) ps.setNull(2, java.sql.Types.BIGINT); else ps.setLong(2, userId);
            ps.setString(3, question);
            ps.setString(4, answer);
            ps.setString(5, toJson(refs));
            ps.setString(6, modelName);
            ps.setString(7, auditStatus);
            ps.setInt(8, riskLevel);
            ps.setString(9, riskLevel > 0 ? "pending" : "auto_passed");
            ps.setLong(10, responseMs);
        });
    }

    private void validateQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw BusinessException.of(400, "问题不能为空");
        }
        if (question.length() > 2000) {
            throw BusinessException.of(400, "问题不能超过2000个字符");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("序列化知识来源失败", e);
        }
    }

    private long countAudit(String keyword, String status) {
        StringBuilder sql = new StringBuilder("select count(*) from chat_session c where 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (c.question like ? or c.answer like ? or c.session_id like ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and (c.audit_status = ? or c.review_status = ?)");
            params.add(status);
            params.add(status);
        }
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private PageResult<Map<String, Object>> page(List<Map<String, Object>> all, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, size);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return PageResult.of(all.subList(from, to), all.size(), safePage, safeSize);
    }

    private double score(String term, String title, String content, String type) {
        String lowerTerm = term.toLowerCase(Locale.ROOT);
        String lowerTitle = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String lowerContent = content == null ? "" : content.toLowerCase(Locale.ROOT);
        double score = 55.0;
        if (!lowerTerm.isBlank() && lowerTitle.contains(lowerTerm)) {
            score += 30;
        }
        if (!lowerTerm.isBlank() && lowerContent.contains(lowerTerm)) {
            score += 12;
        }
        if ("办事服务".equals(type)) {
            score += 4;
        }
        return Math.min(score, 99.0);
    }

    private double semanticBonus(String term, Map<String, Object> item) {
        String text = (item.get("title") + " " + item.get("summary")).toLowerCase(Locale.ROOT);
        double bonus = 0;
        for (String token : term.toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!token.isBlank() && text.contains(token)) {
                bonus += 4;
            }
        }
        if (text.contains("办理") || text.contains("申请") || text.contains("指南")) {
            bonus += 5;
        }
        return bonus;
    }

    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "暂无摘要。";
        }
        String compact = text.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
        return compact.length() > 120 ? compact.substring(0, 120) + "..." : compact;
    }

    private String highlight(String text, String term) {
        if (text == null || term == null || term.isBlank()) {
            return text;
        }
        return text.replace(term, "<mark>" + term + "</mark>");
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value == null ? "" : value;
        return value.substring(0, maxLength);
    }

    private double toDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0;
    }

    private void update(String sql, SqlBinder binder) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private int updateCount(String sql, SqlBinder binder) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private <T> List<T> query(String sql, SqlBinder binder, RowMapper<T> mapper) {
        List<T> rows = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
            }
        } catch (Exception e) {
            return rows;
        }
        return rows;
    }

    @FunctionalInterface
    private interface SqlBinder {
        void bind(PreparedStatement ps) throws Exception;
    }

    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws Exception;
    }
}
