package com.gov.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
// Auto-generated getters/setters
@TableName("cms_attachment")
public class CmsAttachment {
    @TableId(type = IdType.AUTO) private Long id;
    private Long contentId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Integer downloadCount;
    @TableField(fill = FieldFill.INSERT) private Long createdBy;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
}

