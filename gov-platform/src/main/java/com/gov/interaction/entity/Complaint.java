package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投诉实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("complaint")
public class Complaint extends BaseEntity {

    private Long userId;
    private String complaintNo;       // 投诉单号
    private String title;             // 投诉标题
    // 投诉单位（三级）
    private String complaintUnitL1;   // 一级(市州)
    private String complaintUnitL2;   // 二级(部门)
    private String complaintUnitL3;   // 三级(子部门)
    // 问题类型（三级）
    private String problemTypeL1;     // 一级
    private String problemTypeL2;     // 二级
    private String problemTypeL3;     // 三级
    // 投诉人信息
    private String realName;          // 姓名
    private String idCard;            // 身份证号
    private String email;             // 电子邮箱
    private String phone;             // 联系电话
    private String address;           // 联系地址
    // 事件发生地
    private String province;          // 省份
    private String city;              // 城市
    private String district;          // 区县
    private String detailAddress;     // 具体地址
    // 内容
    private String content;           // 投诉内容
    private String attachment;        // 附件(JSON)
    private Boolean isPublic;         // 是否公开
    private Boolean isSecret;         // 是否保密
    // 状态流转
    private String status;            // 待受理/处理中/已答复/已办结
    private String replyContent;      // 答复内容
    private String replyBy;           // 答复人
    private LocalDateTime replyTime;  // 答复时间
    private LocalDate deadline;       // 答复期限
}
