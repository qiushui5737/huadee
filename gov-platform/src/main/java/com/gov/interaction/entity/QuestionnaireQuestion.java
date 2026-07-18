package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C5-问卷题目
 */
@Data
@TableName("questionnaire_question")
public class QuestionnaireQuestion {
    private Long id;
    private Long questionnaireId;
    private String questionText;
    private String questionType;  // single/multiple/text
    private String options;       // JSON数组
    private Boolean required;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
