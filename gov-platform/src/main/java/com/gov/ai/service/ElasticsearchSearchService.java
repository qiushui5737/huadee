package com.gov.ai.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.gov.ai.model.SearchDocument;
import com.gov.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ElasticsearchSearchService {
    private final ElasticsearchClient client;
    private final DataSource dataSource;
    private final EmbeddingVectorService embeddingVectorService;

    @Value("${ai.search.elasticsearch-enabled:true}")
    private boolean enabled;

    @Value("${ai.search.index-name:gov_content_v1}")
    private String indexName;

    public ElasticsearchSearchService(ElasticsearchClient client, DataSource dataSource,
            EmbeddingVectorService embeddingVectorService) {
        this.client = client;
        this.dataSource = dataSource;
        this.embeddingVectorService = embeddingVectorService;
    }

    public boolean isAvailable() {
        if (!enabled) return false;
        try {
            return client.ping().value();
        } catch (Exception e) {
            return false;
        }
    }

    public PageResult<Map<String, Object>> search(String keyword, int page, int size) throws Exception {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        SearchResponse<SearchDocument> response = client.search(s -> s
                .index(indexName)
                .from((safePage - 1) * safeSize)
                .size(safeSize)
                .trackTotalHits(t -> t.enabled(true))
                .query(q -> q.multiMatch(m -> m
                        .query(keyword)
                        .fields("title^5", "summary^3", "content^2", "source", "deptCode", "bizType")
                ))
                .highlight(h -> h
                        .preTags("<mark>")
                        .postTags("</mark>")
                        .fields("title", f -> f.numberOfFragments(0))
                        .fields("summary", f -> f.fragmentSize(180).numberOfFragments(1))
                        .fields("content", f -> f.fragmentSize(180).numberOfFragments(1))
                )
                .sort(sort -> sort.score(sc -> sc.order(SortOrder.Desc)))
                .sort(sort -> sort.field(f -> f.field("createTime").order(SortOrder.Desc))),
                SearchDocument.class
        );

        List<Map<String, Object>> records = new ArrayList<>();
        for (Hit<SearchDocument> hit : response.hits().hits()) {
            SearchDocument doc = hit.source();
            if (doc == null) continue;
            Map<String, Object> row = toResult(doc, hit.score());
            List<String> highlighted = firstHighlight(hit.highlight(), "summary", "content");
            row.put("highlight", highlighted.isEmpty() ? doc.getSummary() : String.join("...", highlighted));
            List<String> titleHighlight = hit.highlight().get("title");
            if (titleHighlight != null && !titleHighlight.isEmpty()) row.put("titleHighlight", titleHighlight.get(0));
            records.add(row);
        }
        long total = response.hits().total() == null ? records.size() : response.hits().total().value();
        return PageResult.of(records, total, safePage, safeSize);
    }

    public PageResult<Map<String, Object>> semanticSearch(String keyword, int page, int size, double minScore)
            throws Exception {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        List<Double> queryVector = embeddingVectorService.embed(keyword);
        String query = objectMapperQuery(safePage, safeSize, minScore, queryVector);
        SearchResponse<SearchDocument> response = client.search(
                s -> s.index(indexName).withJson(new StringReader(query)), SearchDocument.class);

        List<Map<String, Object>> records = new ArrayList<>();
        for (Hit<SearchDocument> hit : response.hits().hits()) {
            SearchDocument doc = hit.source();
            if (doc == null) continue;
            double similarity = hit.score() == null ? 0 : Math.max(-1, hit.score() - 1.0);
            Map<String, Object> row = toResult(doc, similarity);
            row.put("highlight", doc.getSummary());
            row.put("matchMode", "semantic");
            row.put("engine", embeddingVectorService.engine());
            records.add(row);
        }
        long total = response.hits().total() == null ? records.size() : response.hits().total().value();
        return PageResult.of(records, total, safePage, safeSize);
    }

    public synchronized Map<String, Object> rebuild() {
        long started = System.currentTimeMillis();
        try {
            if (client.indices().exists(e -> e.index(indexName)).value()) {
                client.indices().delete(d -> d.index(indexName));
            }
            createIndex();
            List<SearchDocument> documents = loadAllDocuments();
            for (SearchDocument document : documents) {
                document.setEmbedding(embeddingVectorService.embed(
                        document.getTitle() + "\n" + document.getSummary() + "\n" + document.getContent()));
            }
            BulkRequest.Builder bulk = new BulkRequest.Builder();
            for (SearchDocument document : documents) {
                bulk.operations(op -> op.index(idx -> idx
                        .index(indexName)
                        .id(document.getId())
                        .document(document)
                ));
            }
            BulkResponse response = documents.isEmpty() ? null : client.bulk(bulk.build());
            if (response != null && response.errors()) {
                String reason = response.items().stream()
                        .filter(item -> item.error() != null)
                        .map(item -> item.error().reason())
                        .findFirst().orElse("未知批量索引错误");
                throw new IllegalStateException(reason);
            }
            client.indices().refresh(r -> r.index(indexName));
            return Map.of(
                    "success", true,
                    "indexName", indexName,
                    "embeddingEngine", embeddingVectorService.engine(),
                    "documentCount", documents.size(),
                    "elapsedMs", System.currentTimeMillis() - started
            );
        } catch (Exception e) {
            throw new IllegalStateException("Elasticsearch 全量重建失败: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("available", isAvailable());
        result.put("indexName", indexName);
        result.put("embedding", embeddingVectorService.status());
        if (isAvailable()) {
            try {
                boolean exists = client.indices().exists(e -> e.index(indexName)).value();
                result.put("indexExists", exists);
                result.put("documentCount", exists ? client.count(c -> c.index(indexName)).count() : 0L);
            } catch (Exception e) {
                result.put("error", e.getMessage());
            }
        }
        return result;
    }

    public void saveSearchLog(String keyword, String mode, long resultCount, String engine, long elapsedMs) {
        if (isInvalidKeyword(keyword)) {
            log.debug("Skip invalid search keyword from statistics");
            return;
        }
        String sql = "insert into search_log(keyword, search_mode, result_count, engine, elapsed_ms) values(?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, mode);
            ps.setLong(3, resultCount);
            ps.setString(4, engine);
            ps.setLong(5, elapsedMs);
            ps.executeUpdate();
        } catch (Exception e) {
            log.warn("Unable to write search log: {}", e.getMessage());
        }
    }

    public List<String> hotKeywords() {
        String sql = "select keyword, count(*) searches from search_log "
                + "where create_time >= date_sub(now(), interval 30 day) "
                + "and keyword not like '%?%' and keyword not like '%�%' "
                + "group by keyword order by searches desc, max(create_time) desc limit 10";
        List<String> words = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) words.add(rs.getString("keyword"));
        } catch (Exception e) {
            log.warn("Unable to read hot keywords: {}", e.getMessage());
        }
        return words;
    }

    private boolean isInvalidKeyword(String keyword) {
        return keyword == null || keyword.isBlank() || keyword.indexOf('?') >= 0 || keyword.indexOf('\uFFFD') >= 0;
    }

    private void createIndex() throws Exception {
        String mapping = """
            {
              "settings": {"number_of_shards": 1, "number_of_replicas": 0},
              "mappings": {
                "properties": {
                  "id": {"type": "keyword"},
                  "bizType": {"type": "keyword"},
                  "bizId": {"type": "long"},
                  "title": {"type": "text"},
                  "content": {"type": "text"},
                  "summary": {"type": "text"},
                  "source": {"type": "keyword"},
                  "deptCode": {"type": "keyword"},
                  "url": {"type": "keyword", "index": false},
                  "createTime": {"type": "date", "format": "yyyy-MM-dd HH:mm:ss||strict_date_optional_time"},
                  "embedding": {"type": "dense_vector", "dims": %d, "index": true, "similarity": "cosine"}
                }
              }
            }
            """.formatted(embeddingVectorService.dimensions());
        client.indices().create(c -> c.index(indexName).withJson(new StringReader(mapping)));
    }

    private List<SearchDocument> loadAllDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        load(documents, "select id,title,content,source,dept_code,create_time from knowledge_doc where deleted=0 and status=1",
                "知识库", "/chat", "source");
        load(documents, "select id,title,content,category as source,site_code as dept_code,create_time from cms_content where deleted=0 and status in ('published','approved')",
                "信息公开", "/dept", "source");
        load(documents, "select id,item_name as title,description as content,category as source,dept_code,create_time from service_item where deleted=0 and status=1",
                "办事服务", "/service", "source");
        return documents;
    }

    private void load(List<SearchDocument> target, String sql, String type, String url, String sourceColumn) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String content = cleanText(rs.getString("content"));
                Timestamp time = rs.getTimestamp("create_time");
                target.add(new SearchDocument(
                        type + "-" + id,
                        type,
                        id,
                        rs.getString("title"),
                        content,
                        summarize(content),
                        rs.getString(sourceColumn),
                        rs.getString("dept_code"),
                        url,
                        time == null ? LocalDateTime.now().toString() : time.toLocalDateTime().toString(),
                        null
                ));
            }
        } catch (Exception e) {
            throw new IllegalStateException("读取" + type + "索引数据失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> toResult(SearchDocument doc, Double score) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", doc.getBizId());
        row.put("title", doc.getTitle());
        row.put("summary", doc.getSummary());
        row.put("type", doc.getBizType());
        row.put("source", doc.getSource());
        row.put("deptCode", doc.getDeptCode());
        row.put("url", doc.getUrl());
        row.put("score", score == null ? 0 : score);
        row.put("matchMode", "keyword");
        row.put("engine", "elasticsearch");
        row.put("createTime", doc.getCreateTime());
        return row;
    }

    private List<String> firstHighlight(Map<String, List<String>> highlights, String... fields) {
        for (String field : fields) {
            List<String> values = highlights.get(field);
            if (values != null && !values.isEmpty()) return values;
        }
        return List.of();
    }

    private String cleanText(String value) {
        return value == null ? "" : value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String summarize(String value) {
        if (value == null || value.isBlank()) return "暂无摘要。";
        return value.length() > 180 ? value.substring(0, 180) + "..." : value;
    }

    private String objectMapperQuery(int page, int size, double minScore, List<Double> queryVector) {
        try {
            Map<String, Object> script = Map.of(
                    "source", "cosineSimilarity(params.query_vector, 'embedding') + 1.0",
                    "params", Map.of("query_vector", queryVector));
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("from", (page - 1) * size);
            body.put("size", size);
            body.put("track_total_hits", true);
            body.put("min_score", 1.0 + Math.max(-1.0, Math.min(1.0, minScore)));
            body.put("query", Map.of("script_score", Map.of("query", Map.of("match_all", Map.of()), "script", script)));
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build semantic search query", e);
        }
    }
}
