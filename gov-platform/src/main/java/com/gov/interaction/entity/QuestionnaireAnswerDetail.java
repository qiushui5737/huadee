package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C5-答卷详情
 */
@Data
@TableName("questionnaire_answer_detail")
public class QuestionnaireAnswerDetail {
    private Long id;
    private Long answerId;
    private Long questionId;
    private String answerText;      // 文本题答案
    private String answerOptions;   // 选择题答案(JSON数组)
    private LocalDateTime createTime;
}
