package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言回复记录（多轮对话）
 */
@Data
@TableName("message_reply")
public class MessageReply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long messageId;       // 关联的留言ID
    private Long userId;          // 回复用户ID（C端用户）
    private String userName;      // 回复人姓名
    private String userType;      // 回复人类型: ADMIN-管理员, USER-群众
    private String content;       // 回复内容
    private LocalDateTime createTime; // 回复时间
}
