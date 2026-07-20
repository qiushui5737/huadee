package com.gov.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_record")
public class ServiceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("accept_no")
    private String acceptNo;

    @TableField("item_id")
    private Long itemId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("form_data")
    private String formData;

    @TableField("status")
    private String status;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("comment")
    private String comment;

    @TableField("pay_status")
    private String payStatus;

    @TableField("pay_amount")
    private java.math.BigDecimal payAmount;

    @TableField("pay_time")
    private LocalDateTime payTime;

    @TableField("license_status")
    private String licenseStatus;

    @TableField("draft")
    private Integer draft;

    @TableField("current_stage")
    private String currentStage;

    @TableField("supplement_reason")
    private String supplementReason;

    @TableField("supplement_status")
    private String supplementStatus;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("rating")
    private Integer rating;

    @TableField("rating_content")
    private String ratingContent;
}