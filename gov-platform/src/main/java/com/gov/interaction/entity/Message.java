package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * C1-政民互动留言实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message")
public class Message extends BaseEntity {

    private Long userId;
    private String userName;
    private String title;
    private String content;
    private String deptCode;
    private String status;
    private String replyContent;
    private String replyBy;
    private LocalDateTime replyTime;
}
