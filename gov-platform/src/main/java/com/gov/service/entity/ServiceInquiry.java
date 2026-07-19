package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_inquiry")
public class ServiceInquiry {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("accept_no")
    private String acceptNo;

    @TableField("content")
    private String content;

    @TableField("reply")
    private String reply;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("reply_time")
    private LocalDateTime replyTime;
}