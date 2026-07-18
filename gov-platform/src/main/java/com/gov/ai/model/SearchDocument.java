package com.gov.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDocument {
    private String id;
    private String bizType;
    private Long bizId;
    private String title;
    private String content;
    private String summary;
    private String source;
    private String deptCode;
    private String url;
    private String createTime;
    private List<Double> embedding;
}
