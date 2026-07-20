package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件访问权限实体
 */
@Data
@TableName("disclosure_file_access")
public class DisclosureFileAccess {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fileId;          // 关联文件ID
    private String applyNo;       // 关联申请编号
    private Long userId;          // 申请人ID
    private String userName;      // 申请人姓名
    private String accessType;    // 访问类型: view/download
    private Long grantedBy;       // 授权人ID
    private String grantedName;   // 授权人姓名
    private LocalDateTime grantTime;    // 授权时间
    private LocalDateTime expireTime;   // 过期时间
    private String status;        // active/expired/revoked
}
