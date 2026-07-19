package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我要咨询 - 咨询实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation")
public class Consultation extends BaseEntity {

    private Long userId;          // 提交用户ID
    private String consultNo;       // 咨询单号
    private String realName;        // 真实姓名
    private String phone;           // 手机号码
    private String telephone;       // 电话号码
    private String email;           // 电子邮箱
    private String title;           // 咨询主题
    private String city;            // 咨询市州
    private String district;        // 事件发生地-区
    private String street;          // 事件发生地-街道
    private String address;         // 事件发生具体地址
    private String attachment;      // 附件路径
    private String content;         // 咨询内容
    private Boolean isPublic;       // 是否愿意公开
    private Boolean isConfidential; // 是否保密
    private String status;          // 待受理/处理中/已答复/已办结
    private String replyContent;    // 答复内容
    private String replyBy;         // 答复人
    private LocalDateTime replyTime;// 答复时间
    private LocalDate deadline;     // 答复期限
}
