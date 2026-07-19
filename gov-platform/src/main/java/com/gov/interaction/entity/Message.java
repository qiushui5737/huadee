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
    private String type;          // 留言类型: 咨询/投诉/建议/求助
    private String content;
    private String deptCode;
    private String status;        // 待分派/已分派/已回复/已办结
    private Boolean supervise;    // 是否督办
    private LocalDateTime superviseTime;  // 督办时间
    private LocalDateTime deadline;       // 处理期限
    private String replyContent;
    private String replyBy;
    private LocalDateTime replyTime;
    private Integer rating;       // 评价星级 1-5
    private String ratingContent; // 评价内容
    private Boolean isPublic;     // 是否公开: true-公开, false-私密
    private String consultNo;     // 信件单号
}
