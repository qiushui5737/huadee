package com.gov.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流程记录实体
 */
@Data
@TableName("disclosure_audit_record")
public class DisclosureAuditRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applyNo;       // 关联申请编号
    private String nodeName;      // 流程节点名称
    private Long operatorId;      // 操作人ID
    private String operatorName;  // 操作人姓名
    private String action;        // 操作动作: accept/processing/approve/reject
    private String opinion;       // 审批意见
    private LocalDateTime operateTime; // 操作时间
}
