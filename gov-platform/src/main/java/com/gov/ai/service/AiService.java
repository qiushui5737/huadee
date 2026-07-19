package com.gov.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gov.common.result.PageResult;
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

    public AiService(DataSource dataSource, ObjectMapper objectMapper) {
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public PageResult<Map<String, Object>> search(String keyword, int page, int size) {
        String term = clean(keyword);
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(searchKnowledge(term));
        all.addAll(searchCms(term));
        all.addAll(searchServices(term));
        all.sort(Comparator.comparingDouble(item -> -toDouble(item.get("score"))));
        return page(all, page, size);
    }

    public PageResult<Map<String, Object>> semanticSearch(String keyword, int page, int size) {
        String term = clean(keyword);
        List<Map<String, Object>> all = new ArrayList<>();
        all.addAll(searchKnowledge(term));
        all.addAll(searchCms(term));
        all.addAll(searchServices(term));
        for (Map<String, Object> item : all) {
            item.put("matchMode", "semantic");
            item.put("score", Math.min(99.0, toDouble(item.get("score")) + semanticBonus(term, item)));
        }
        all.sort(Comparator.comparingDouble(item -> -toDouble(item.get("score"))));
        return page(all, page, size);
    }

    public List<String> hotKeywords() {
        return List.of("学生资助", "健康证明", "政府信息公开", "社保参保证明", "办事进度");
    }

    public Map<String, Object> checkSensitive(String text) {
        String source = text == null ? "" : text;
        List<Map<String, Object>> words = sensitiveWords();
        List<Map<String, Object>> hits = new ArrayList<>();
        String filtered = source;

        for (Map<String, Object> wordInfo : words) {
            String word = String.valueOf(wordInfo.get("word"));
            if (!word.isBlank() && source.toLowerCase(Locale.ROOT).contains(word.toLowerCase(Locale.ROOT))) {
                hits.add(wordInfo);
                filtered = filtered.replaceAll("(?i)" + java.util.regex.Pattern.quote(word), "*".repeat(word.length()));
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passed", hits.isEmpty());
        result.put("filtered", filtered);
        result.put("hits", hits);
        result.put("hitCount", hits.size());
        result.put("algorithm", "DFA-local");
        return result;
    }

    public List<Map<String, Object>> sensitiveWords() {
        String sql = "select id, word, category, level, status from sensitive_word where deleted = 0 and status = 1 order by level desc, id desc";
        return query(sql, ps -> {}, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("word", rs.getString("word"));
            row.put("category", rs.getString("category"));
            row.put("level", rs.getInt("level"));
            row.put("status", rs.getInt("status"));
            return row;
        });
    }

    public Map<String, Object> answer(String question, String sessionId) {
        String safeSession = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : sessionId;
        Map<String, Object> sensitive = checkSensitive(question);
        List<Map<String, Object>> refs = semanticSearch(question, 1, 4).getRecords();
        boolean passed = Boolean.TRUE.equals(sensitive.get("passed"));
        String answer = passed ? buildAnswer(question, refs) : sensitiveTip();
        String modelName = "local-rag-demo";

        if (passed && realAiConfigured()) {
            try {
                answer = callOpenAiCompatibleChat(question, refs);
                modelName = aiChatModel;
            } catch (Exception e) {
                answer = answer + "\n\n提示：真实 AI 接口调用失败，已自动使用本地知识库回答。失败原因：" + e.getMessage();
            }
        }

        saveChat(safeSession, question, answer, refs, modelName, passed ? "normal" : "sensitive");

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
            Consumer<Map<String, Object>> metaConsumer,
            Consumer<String> chunkConsumer) {
        String safeSession = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : sessionId;
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
                answer = callOpenAiCompatibleChatStream(question, refs, chunkConsumer);
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

        saveChat(safeSession, question, answer, refs, modelName, passed ? "normal" : "sensitive");

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

    public List<Map<String, Object>> history(String sessionId) {
        String sql = "select id, session_id, question, answer, sources, audit_status, create_time from chat_session where session_id = ? order by id asc limit 50";
        return query(sql, ps -> ps.setString(1, sessionId), rs -> {
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

    public PageResult<Map<String, Object>> auditList(String keyword, String status, int page, int size) {
        StringBuilder sql = new StringBuilder("select id, session_id, question, answer, audit_status, create_time from chat_session where 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (question like ? or answer like ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and audit_status = ?");
            params.add(status);
        }
        sql.append(" order by id desc limit ? offset ?");
        params.add(size);
        params.add(Math.max(0, (page - 1) * size));

        List<Map<String, Object>> records = query(sql.toString(), ps -> {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
        }, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("sessionId", rs.getString("session_id"));
            row.put("question", rs.getString("question"));
            row.put("answer", rs.getString("answer"));
            row.put("auditStatus", rs.getString("audit_status"));
            row.put("createTime", String.valueOf(rs.getTimestamp("create_time")));
            return row;
        });
        return PageResult.of(records, countAudit(keyword, status), page, size);
    }

    private boolean realAiConfigured() {
        return aiEnabled && !clean(aiBaseUrl).isBlank() && !clean(aiApiKey).isBlank();
    }

    private String callOpenAiCompatibleChat(String question, List<Map<String, Object>> refs) throws Exception {
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
        payload.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt()),
            Map.of("role", "user", "content", buildRagPrompt(question, refs))
        ));

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
            Consumer<String> chunkConsumer) throws Exception {
        String endpoint = chatCompletionEndpoint();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", aiChatModel);
        payload.put("temperature", 0.2);
        payload.put("stream", true);
        payload.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt()),
            Map.of("role", "user", "content", buildRagPrompt(question, refs))
        ));

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
        return "你是政府网站集约化平台的政务智能助手。\n" +
            "回答要求：\n" +
            "1. 使用中文，语气正式、清晰、友好。\n" +
            "2. 优先参考提供的知识库资料回答。\n" +
            "3. 知识库资料不足时，可以基于通用政务知识进行回答。\n" +
            "4. 不能编造具体政策编号、办理时限、收费标准、咨询电话和办理地点。\n" +
            "5. 如果涉及材料、流程、注意事项，尽量用条目或表格说明。\n" +
            "6. 对无法确认的具体事项，要提示用户以当地政府网站、政务服务平台或主管部门答复为准。\n" +
            "7. 不输出与政务咨询无关的内容。";
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

    private void saveChat(String sessionId, String question, String answer, List<Map<String, Object>> refs, String modelName, String auditStatus) {
        String sql = "insert into chat_session(session_id, user_id, question, answer, sources, model_name, audit_status) values(?, null, ?, ?, ?, ?, ?)";
        update(sql, ps -> {
            ps.setString(1, sessionId);
            ps.setString(2, question);
            ps.setString(3, answer);
            ps.setString(4, refs.toString());
            ps.setString(5, modelName);
            ps.setString(6, auditStatus);
        });
    }

    private long countAudit(String keyword, String status) {
        StringBuilder sql = new StringBuilder("select count(*) from chat_session where 1=1");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (question like ? or answer like ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and audit_status = ?");
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
