package com.gov.ai.service;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SensitiveWordServiceTest {

    @Test
    void detectsBothDirectionsAndAppliesLevelPolicy() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        AtomicInteger row = new AtomicInteger(-1);
        String[] words = {"replaceword", "reviewword", "blockword"};
        int[] levels = {1, 2, 3};

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenAnswer(ignored -> row.incrementAndGet() < words.length);
        when(resultSet.getLong("id")).thenAnswer(ignored -> (long) row.get() + 1);
        when(resultSet.getString("word")).thenAnswer(ignored -> words[row.get()]);
        when(resultSet.getString("category")).thenReturn("test");
        when(resultSet.getInt("level")).thenAnswer(ignored -> levels[row.get()]);

        SensitiveWordService service = new SensitiveWordService(dataSource);
        Map<String, Object> replace = service.check("prefix replaceword suffix");
        Map<String, Object> review = service.check("reviewword");
        Map<String, Object> block = service.check("blockword");

        assertThat(replace.get("action")).isEqualTo("replace");
        assertThat(replace.get("filtered")).isEqualTo("prefix *********** suffix");
        assertThat(review.get("action")).isEqualTo("review");
        assertThat(block.get("action")).isEqualTo("block");
        assertThat(block.get("allowed")).isEqualTo(false);
        @SuppressWarnings("unchecked")
        Map<String, Object> hit = ((java.util.List<Map<String, Object>>) block.get("hits")).get(0);
        assertThat(hit.get("directions")).isEqualTo("forward,reverse");
    }
}
