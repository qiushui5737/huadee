package com.gov.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
// Auto-generated getters/setters
@TableName("cms_column")
public class CmsColumn {
    @TableId(type = IdType.AUTO) private Long id;
    private String siteCode;
    private Long parentId;
    private String columnName;
    private String columnType;
    private String linkUrl;
    private Integer sortOrder;
    private Integer isShow;
    private String template;
    private String seoKeywords;
    private String seoDescription;

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
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    public String getColumnType() { return columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getIsShow() { return isShow; }
    public void setIsShow(Integer isShow) { this.isShow = isShow; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getSeoKeywords() { return seoKeywords; }
    public void setSeoKeywords(String seoKeywords) { this.seoKeywords = seoKeywords; }
    public String getSeoDescription() { return seoDescription; }
    public void setSeoDescription(String seoDescription) { this.seoDescription = seoDescription; }    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}