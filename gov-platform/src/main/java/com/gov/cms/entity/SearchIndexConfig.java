package com.gov.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
// Auto-generated getters/setters
@TableName("search_index_config")
public class SearchIndexConfig {
    @TableId(type = IdType.AUTO) private Long id;
    private String indexName;
    private String displayName;
    private String dataSource;
    private String syncStrategy;
    private LocalDateTime lastSyncAt;
    private Integer docCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getSyncStrategy() { return syncStrategy; }
    public void setSyncStrategy(String syncStrategy) { this.syncStrategy = syncStrategy; }
    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public Integer getDocCount() { return docCount; }
    public void setDocCount(Integer docCount) { this.docCount = docCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

