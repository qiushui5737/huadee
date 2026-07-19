package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gov.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 意见征集-意见提交实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("collection_opinion")
public class CollectionOpinion extends BaseEntity {

    private Long collectionId;        // 关联征集ID
    private Long userId;              // 提交用户ID
    private String title;             // 意见标题
    private String realName;          // 姓名
    private String phone;             // 手机号码
    private String idCard;            // 身份证号
    private String address;           // 通信地址
    private String content;           // 留言内容
    private String status;            // 待处理/已回复
    private String replyContent;      // 回复内容
    private String replyBy;           // 回复人
    private LocalDateTime replyTime;  // 回复时间
}
