package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * C5-问卷主表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("questionnaire")
public class Questionnaire extends BaseEntity {
    private String title;
    private String description;
    private String status;        // 草稿/已发布/已关闭
    private LocalDateTime publishTime;
    private LocalDateTime closeTime;
    private Integer totalAnswers;
    private String createBy;
}
