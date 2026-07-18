package com.gov.ai.service;

import com.gov.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SensitiveWordService {
    private final DataSource dataSource;
    private volatile TrieCache cache = new TrieCache(new TrieNode(), new TrieNode(), List.of(), 0);

    public SensitiveWordService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> check(String source) {
        String text = source == null ? "" : source;
        TrieCache current = cache();
        int[] points = text.toLowerCase(Locale.ROOT).codePoints().toArray();
        Map<String, Match> matches = new LinkedHashMap<>();
        collectForward(points, current.forward, matches);
        collectReverse(points, current.reverse, matches);
        List<Match> ordered = matches.values().stream()
                .sorted(Comparator.comparingInt(Match::start).thenComparingInt(Match::end))
                .toList();
        int maxLevel = ordered.stream().mapToInt(match -> match.word.level).max().orElse(0);
        String action = maxLevel >= 3 ? "block" : maxLevel == 2 ? "review" : maxLevel == 1 ? "replace" : "pass";

        List<Map<String, Object>> hits = new ArrayList<>();
        for (Match match : ordered) {
            hits.add(Map.of(
                    "id", match.word.id,
                    "word", match.word.word,
                    "category", match.word.category == null ? "default" : match.word.category,
                    "level", match.word.level,
                    "start", match.start,
                    "end", match.end,
                    "directions", String.join(",", match.directions)
            ));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passed", ordered.isEmpty());
        result.put("allowed", maxLevel < 3);
        result.put("action", action);
        result.put("maxLevel", maxLevel);
        result.put("filtered", mask(text, ordered));
        result.put("hits", hits);
        result.put("hitCount", hits.size());
        result.put("algorithm", "DFA-bidirectional-max-match");
        return result;
    }

    public String filter(String text) {
        return String.valueOf(check(text).get("filtered"));
    }

    public void assertNotBlocked(String text) {
        Map<String, Object> result = check(text);
        if (!Boolean.TRUE.equals(result.get("allowed"))) {
            throw BusinessException.of(400, "内容包含禁止发布的敏感词，请修改后重试");
        }
    }

    public List<Map<String, Object>> words() {
        return cache().words.stream().map(word -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", word.id);
            row.put("word", word.word);
            row.put("category", word.category);
            row.put("level", word.level);
            row.put("status", 1);
            return row;
        }).toList();
    }

    public Map<String, Object> create(Map<String, Object> body) {
        String word = required(body.get("word"));
        int level = level(body.get("level"));
        String category = body.get("category") == null ? "default" : String.valueOf(body.get("category"));
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "insert into sensitive_word(word,category,level,match_type,status,deleted) values(?,?,?,'dfa-bidirectional',1,0)",
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, word);
            ps.setString(2, category);
            ps.setInt(3, level);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : 0;
                invalidate();
                return Map.of("id", id, "word", word, "category", category, "level", level);
            }
        } catch (Exception e) {
            throw new IllegalStateException("新增敏感词失败: " + e.getMessage(), e);
        }
    }

    public void update(long id, Map<String, Object> body) {
        String word = required(body.get("word"));
        int level = level(body.get("level"));
        String category = body.get("category") == null ? "default" : String.valueOf(body.get("category"));
        execute("update sensitive_word set word=?,category=?,level=?,match_type='dfa-bidirectional',status=1,deleted=0 where id=?",
                ps -> { ps.setString(1, word); ps.setString(2, category); ps.setInt(3, level); ps.setLong(4, id); });
        invalidate();
    }

    public void delete(long id) {
        execute("update sensitive_word set deleted=1,status=0 where id=?", ps -> ps.setLong(1, id));
        invalidate();
    }

    public void invalidate() {
        cache = new TrieCache(new TrieNode(), new TrieNode(), List.of(), 0);
    }

    private TrieCache cache() {
        TrieCache current = cache;
        if (current.loadedAt > 0 && System.currentTimeMillis() - current.loadedAt < 60_000) return current;
        synchronized (this) {
            if (cache.loadedAt > 0 && System.currentTimeMillis() - cache.loadedAt < 60_000) return cache;
            cache = load();
            return cache;
        }
    }

    private TrieCache load() {
        TrieNode forward = new TrieNode();
        TrieNode reverse = new TrieNode();
        List<WordInfo> words = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "select id,word,category,level from sensitive_word where deleted=0 and status=1 order by level desc,id desc");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                WordInfo info = new WordInfo(rs.getLong("id"), rs.getString("word"), rs.getString("category"), rs.getInt("level"));
                words.add(info);
                insert(forward, info, false);
                insert(reverse, info, true);
            }
        } catch (Exception e) {
            throw new IllegalStateException("加载敏感词失败: " + e.getMessage(), e);
        }
        return new TrieCache(forward, reverse, words, System.currentTimeMillis());
    }

    private void insert(TrieNode root, WordInfo info, boolean reversed) {
        int[] points = info.word.toLowerCase(Locale.ROOT).codePoints().toArray();
        TrieNode node = root;
        for (int offset = 0; offset < points.length; offset++) {
            int index = reversed ? points.length - 1 - offset : offset;
            node = node.children.computeIfAbsent(points[index], ignored -> new TrieNode());
        }
        node.word = info;
    }

    private void collectForward(int[] points, TrieNode root, Map<String, Match> matches) {
        for (int start = 0; start < points.length; start++) {
            TrieNode node = root;
            Match longest = null;
            for (int end = start; end < points.length; end++) {
                node = node.children.get(points[end]);
                if (node == null) break;
                if (node.word != null) longest = new Match(node.word, start, end + 1, new ArrayList<>(List.of("forward")));
            }
            merge(matches, longest);
        }
    }

    private void collectReverse(int[] points, TrieNode root, Map<String, Match> matches) {
        for (int end = points.length - 1; end >= 0; end--) {
            TrieNode node = root;
            Match longest = null;
            for (int start = end; start >= 0; start--) {
                node = node.children.get(points[start]);
                if (node == null) break;
                if (node.word != null) longest = new Match(node.word, start, end + 1, new ArrayList<>(List.of("reverse")));
            }
            merge(matches, longest);
        }
    }

    private void merge(Map<String, Match> matches, Match candidate) {
        if (candidate == null) return;
        String key = candidate.word.id + ":" + candidate.start + ":" + candidate.end;
        Match existing = matches.get(key);
        if (existing == null) matches.put(key, candidate);
        else candidate.directions.forEach(direction -> { if (!existing.directions.contains(direction)) existing.directions.add(direction); });
    }

    private String mask(String text, List<Match> matches) {
        int[] points = text.codePoints().toArray();
        boolean[] masked = new boolean[points.length];
        for (Match match : matches) for (int i = match.start; i < match.end && i < masked.length; i++) masked[i] = true;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < points.length; i++) result.appendCodePoint(masked[i] ? '*' : points[i]);
        return result.toString();
    }

    private String required(Object value) {
        String result = value == null ? "" : String.valueOf(value).trim();
        if (result.isBlank() || result.length() > 100) throw BusinessException.of(400, "敏感词长度必须为1到100个字符");
        return result;
    }

    private int level(Object value) {
        int result;
        try { result = value == null ? 1 : Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException e) { throw BusinessException.of(400, "敏感等级必须为1到3"); }
        if (result < 1 || result > 3) throw BusinessException.of(400, "敏感等级必须为1到3");
        return result;
    }

    private void execute(String sql, Binder binder) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            if (ps.executeUpdate() == 0) throw BusinessException.of(404, "敏感词不存在");
        } catch (BusinessException e) { throw e; }
        catch (Exception e) { throw new IllegalStateException(e.getMessage(), e); }
    }

    private static final class TrieNode { private final Map<Integer, TrieNode> children = new HashMap<>(); private WordInfo word; }
    private record WordInfo(long id, String word, String category, int level) {}
    private record TrieCache(TrieNode forward, TrieNode reverse, List<WordInfo> words, long loadedAt) {}
    private record Match(WordInfo word, int start, int end, List<String> directions) {}
    @FunctionalInterface private interface Binder { void bind(PreparedStatement ps) throws Exception; }
}
