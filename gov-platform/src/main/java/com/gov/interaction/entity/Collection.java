package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 意见征集实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collection")
public class Collection extends BaseEntity {

    private String title;             // 征集主题
    private String description;       // 征集说明
    private String deptName;          // 征集单位
    private String contactName;       // 联系人
    private String contactPhone;      // 联系电话
    private String attachmentName;    // 附件名称
    private String attachmentUrl;     // 附件路径
    private LocalDate startDate;      // 征集开始日期
    private LocalDate endDate;        // 征集结束日期
    private String status;            // 进行中/已结束
    private String feedback;          // 结果反馈内容
    private LocalDateTime feedbackTime; // 反馈时间
}
