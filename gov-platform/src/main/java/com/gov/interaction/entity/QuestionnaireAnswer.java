package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C5-答卷
 */
@Data
@TableName("questionnaire_answer")
public class QuestionnaireAnswer {
    private Long id;
    private Long questionnaireId;
    private String userName;
    private String phone;
    private LocalDateTime submitTime;
}
