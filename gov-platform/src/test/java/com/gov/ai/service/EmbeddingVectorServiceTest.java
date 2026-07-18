package com.gov.ai.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingVectorServiceTest {

    @Test
    void localVectorIsStableAndNormalized() {
        EmbeddingVectorService service = new EmbeddingVectorService();
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "dimensions", 384);

        List<Double> first = service.embed("学生资助申请");
        List<Double> second = service.embed("学生资助申请");
        double norm = Math.sqrt(first.stream().mapToDouble(value -> value * value).sum());

        assertThat(first).hasSize(384).isEqualTo(second);
        assertThat(norm).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.000001));
        assertThat(service.engine()).isEqualTo("local-feature-vector");
    }
}
