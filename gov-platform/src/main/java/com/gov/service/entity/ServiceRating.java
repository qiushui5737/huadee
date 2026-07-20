package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_rating")
public class ServiceRating {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("accept_no")
    private String acceptNo;

    @TableField("user_id")
    private Long userId;

    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;
}
