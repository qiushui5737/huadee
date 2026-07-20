package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.cms.entity.CmsContent;
import com.gov.cms.entity.ContentAuditRecord;
import com.gov.cms.mapper.CmsContentMapper;
import com.gov.cms.mapper.ContentAuditRecordMapper;
import com.gov.cms.service.ContentAuditRecordService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ContentAuditRecordServiceImpl implements ContentAuditRecordService {
    private final ContentAuditRecordMapper auditMapper;
    private final CmsContentMapper contentMapper;
    public ContentAuditRecordServiceImpl(ContentAuditRecordMapper auditMapper, CmsContentMapper contentMapper) {
        this.auditMapper = auditMapper; this.contentMapper = contentMapper;
    }

    @Override public IPage<ContentAuditRecord> page(int page, int size, Long contentId) {
        LambdaQueryWrapper<ContentAuditRecord> q = new LambdaQueryWrapper<>();
        if (contentId != null) q.eq(ContentAuditRecord::getContentId, contentId);
        q.orderByDesc(ContentAuditRecord::getCreatedAt);
        return auditMapper.selectPage(new Page<>(page, size), q);
    }

    @Override public boolean audit(Long contentId, String action, String opinion, Long operatorId, String operatorName) {
        // 写入审核记录
        ContentAuditRecord record = new ContentAuditRecord();
        record.setContentId(contentId);
        record.setAuditNode("初审");
        record.setAction(action);
        record.setOpinion(opinion);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setCreatedAt(LocalDateTime.now());
        auditMapper.insert(record);

        // 更新内容状态
        CmsContent content = contentMapper.selectById(contentId);
        if (content == null) return false;
        if ("approve".equals(action)) {
            content.setStatus("published");
            content.setPublishAt(LocalDateTime.now());
        } else if ("reject".equals(action)) {
            content.setStatus("rejected");
        } else if ("return".equals(action)) {
            content.setStatus("draft");
        }
        return contentMapper.updateById(content) > 0;
    }
}