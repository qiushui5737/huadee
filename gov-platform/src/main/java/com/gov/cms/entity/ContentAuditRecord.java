package com.gov.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
// Auto-generated getters/setters
@TableName("content_audit_record")
public class ContentAuditRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long contentId;
    private String auditNode;
    private String action;
    private String opinion;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getAuditNode() { return auditNode; }
    public void setAuditNode(String auditNode) { this.auditNode = auditNode; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOpinion() { return opinion; }
    public void setOpinion(String opinion) { this.opinion = opinion; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

