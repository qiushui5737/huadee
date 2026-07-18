package com.gov.ai.service;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingClient;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EmbeddingVectorService {
    @Value("${ai.embedding.enabled:false}")
    private boolean enabled;

    @Value("${ai.embedding.base-url:}")
    private String baseUrl;

    @Value("${ai.embedding.api-key:}")
    private String apiKey;

    @Value("${ai.embedding.model:text-embedding-3-small}")
    private String model;

    @Value("${ai.embedding.dimensions:384}")
    private int dimensions;

    private volatile EmbeddingClient embeddingClient;

    public List<Double> embed(String text) {
        if (isRemoteConfigured()) {
            List<Double> vector = client().embed(text == null ? "" : text);
            if (vector.size() != dimensions) {
                throw new IllegalStateException("Embedding dimension mismatch: configured " + dimensions
                        + ", provider returned " + vector.size());
            }
            return normalize(vector);
        }
        return localFeatureVector(text);
    }

    public int dimensions() {
        return Math.max(32, dimensions);
    }

    public boolean isRemoteConfigured() {
        return enabled && !blank(baseUrl) && !blank(apiKey) && !blank(model);
    }

    public String engine() {
        return isRemoteConfigured() ? "spring-ai-openai" : "local-feature-vector";
    }

    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("remoteConfigured", isRemoteConfigured());
        result.put("engine", engine());
        result.put("model", isRemoteConfigured() ? model : "local-feature-vector");
        result.put("dimensions", dimensions());
        return result;
    }

    private EmbeddingClient client() {
        EmbeddingClient current = embeddingClient;
        if (current != null) return current;
        synchronized (this) {
            if (embeddingClient == null) {
                OpenAiEmbeddingOptions options = new OpenAiEmbeddingOptions();
                options.setModel(model);
                embeddingClient = new OpenAiEmbeddingClient(
                        new OpenAiApi(trimTrailingSlash(baseUrl), apiKey),
                        org.springframework.ai.document.MetadataMode.EMBED,
                        options,
                        RetryTemplate.defaultInstance());
            }
            return embeddingClient;
        }
    }

    private List<Double> localFeatureVector(String source) {
        int size = dimensions();
        double[] values = new double[size];
        String text = Normalizer.normalize(source == null ? "" : source, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        List<String> features = new ArrayList<>();
        for (String token : text.split("[^\\p{L}\\p{N}]+")) {
            if (!token.isBlank()) features.add("w:" + token);
        }
        int[] codePoints = text.codePoints().filter(Character::isLetterOrDigit).toArray();
        for (int i = 0; i < codePoints.length; i++) {
            features.add("c:" + new String(codePoints, i, 1));
            if (i + 1 < codePoints.length) features.add("b:" + new String(codePoints, i, 2));
        }
        for (String feature : features) {
            int hash = feature.hashCode();
            int index = Math.floorMod(hash, size);
            values[index] += (hash & 1) == 0 ? 1.0 : -1.0;
        }
        List<Double> vector = new ArrayList<>(size);
        double norm = 0;
        for (double value : values) norm += value * value;
        norm = Math.sqrt(norm);
        for (double value : values) vector.add(norm == 0 ? 0.0 : value / norm);
        return vector;
    }

    private List<Double> normalize(List<Double> source) {
        double norm = Math.sqrt(source.stream().mapToDouble(v -> v * v).sum());
        if (norm == 0) return source;
        return source.stream().map(v -> v / norm).toList();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String trimTrailingSlash(String value) {
        String result = value.trim();
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }
}
