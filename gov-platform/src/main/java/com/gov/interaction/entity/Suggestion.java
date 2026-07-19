package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 建议实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("suggestion")
public class Suggestion extends BaseEntity {

    private Long userId;
    private String suggestNo;       // 建议单号
    private String title;           // 建议标题
    private String type;            // 建议类型: 网站建议/部门建议/我要纠错/助企惠企服务专区建议
    private String realName;        // 建议人姓名
    private String idCard;          // 身份证号
    private String email;           // 电子邮箱
    private String phone;           // 联系电话
    private String address;         // 联系地址
    private String province;        // 省份
    private String city;            // 城市
    private String district;        // 区县
    private String detailAddress;   // 具体地址
    private String content;         // 建议内容
    private Boolean isPublic;       // 是否公开
    private Boolean isSecret;       // 是否保密
    private String status;          // 状态: 待受理/处理中/已答复/已办结
    private String replyContent;    // 答复内容
    private String replyBy;         // 答复人
    private LocalDateTime replyTime;// 答复时间
    private LocalDate deadline;     // 答复期限
}
