package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * C3-依申请公开实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("disclosure_apply")
public class DisclosureApply extends BaseEntity {

    private String applyNo;       // 申请编号
    private String applicant;     // 申请人
    private String idCard;        // 身份证号
    private String phone;         // 联系电话
    private String content;       // 申请内容
    private String purpose;       // 用途说明
    private String acquireMethod; // 获取方式: 邮寄/电子邮件/自行领取
    private String status;        // 已受理/审核中/已答复/已驳回
    private LocalDate deadline;   // 答复期限(15个工作日)
    private String replyContent;  // 答复内容
    private String replyBy;       // 答复人
    private LocalDateTime replyTime; // 答复时间
}
