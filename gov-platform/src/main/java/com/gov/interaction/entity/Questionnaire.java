package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * C5-问卷主表
 */
@Data
@TableName("questionnaire")
public class Questionnaire {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String status;        // 草稿/已发布/已关闭
    private LocalDateTime publishTime;
    private LocalDateTime closeTime;
    private Integer totalAnswers;
    private String createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
