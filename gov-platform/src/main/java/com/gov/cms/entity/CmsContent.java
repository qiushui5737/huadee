package com.gov.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
// Auto-generated getters/setters
@TableName("cms_content")
public class CmsContent {
    @TableId(type = IdType.AUTO) private Long id;
    private String siteCode;
    private Long columnId;
    private String title;
    private String subtitle;
    private String content;
    private String source;
    private String author;
    private String contentType;
    private String tags;
    private String summary;
    private String coverUrl;
    private String attachments;
    private Integer isTop;
    private LocalDateTime topExpireAt;
    private Integer sortOrder;
    private String status;
    private LocalDateTime publishAt;
    private LocalDateTime offlineAt;
    private Integer viewCount;
    private Integer esSyncStatus;

    @TableField(fill = FieldFill.INSERT) private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private Long updatedBy;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
    private Integer deleted;
    private Integer version;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSiteCode() { return siteCode; }
    public void setSiteCode(String siteCode) { this.siteCode = siteCode; }
    public Long getColumnId() { return columnId; }
    public void setColumnId(Long columnId) { this.columnId = columnId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public LocalDateTime getTopExpireAt() { return topExpireAt; }
    public void setTopExpireAt(LocalDateTime topExpireAt) { this.topExpireAt = topExpireAt; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishAt() { return publishAt; }
    public void setPublishAt(LocalDateTime publishAt) { this.publishAt = publishAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getOfflineAt() { return offlineAt; }
    public void setOfflineAt(LocalDateTime offlineAt) { this.offlineAt = offlineAt; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getEsSyncStatus() { return esSyncStatus; }
    public void setEsSyncStatus(Integer esSyncStatus) { this.esSyncStatus = esSyncStatus; }    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}