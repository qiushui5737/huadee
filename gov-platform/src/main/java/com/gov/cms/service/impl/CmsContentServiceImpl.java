package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.cms.entity.CmsContent;
import com.gov.cms.entity.ContentAuditRecord;
import com.gov.cms.mapper.CmsContentMapper;
import com.gov.cms.mapper.ContentAuditRecordMapper;
import com.gov.cms.service.CmsContentService;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;
import java.time.LocalDateTime;

@Service
public class CmsContentServiceImpl implements CmsContentService {
    private final CmsContentMapper mapper;
    private final ContentAuditRecordMapper auditMapper;
    public CmsContentServiceImpl(CmsContentMapper mapper, ContentAuditRecordMapper auditMapper) {
        this.mapper = mapper; this.auditMapper = auditMapper;
    }

    @Override public IPage<CmsContent> page(int page, int size, String siteCode, String status, String keyword) {
        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(siteCode)) q.eq(CmsContent::getSiteCode, siteCode);
        if (StrUtil.isNotBlank(status)) q.eq(CmsContent::getStatus, status);
        if (StrUtil.isNotBlank(keyword))
            q.like(CmsContent::getTitle, keyword).or().like(CmsContent::getContent, keyword);
        q.orderByDesc(CmsContent::getCreatedAt);
        return mapper.selectPage(new Page<>(page, size), q);
    }
    @Override public CmsContent getById(Long id) { return mapper.selectById(id); }
    @Override public boolean save(CmsContent content) { return mapper.insert(content) > 0; }
    @Override public boolean update(CmsContent content) { return mapper.updateById(content) > 0; }
    @Override public boolean delete(Long id) { return mapper.deleteById(id) > 0; }

    @Override public boolean publish(Long id) {
        CmsContent c = mapper.selectById(id);
        if (c == null) return false;
        c.setStatus("published");
        c.setPublishAt(LocalDateTime.now());
        return mapper.updateById(c) > 0;
    }
    @Override public boolean offline(Long id) {
        CmsContent c = mapper.selectById(id);
        if (c == null) return false;
        c.setStatus("offline");
        c.setOfflineAt(LocalDateTime.now());
        return mapper.updateById(c) > 0;
    }
    @Override public boolean updateEsSyncStatus(Long id, int status) {
        CmsContent c = mapper.selectById(id);
        if (c == null) return false;
        c.setEsSyncStatus(status);
        return mapper.updateById(c) > 0;
    }
}
