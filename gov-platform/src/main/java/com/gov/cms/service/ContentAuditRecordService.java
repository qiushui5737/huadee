package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;

public interface ContentAuditRecordService {
    IPage<ContentAuditRecord> page(int page, int size, Long contentId);
    boolean audit(Long contentId, String action, String opinion, Long operatorId, String operatorName);
}
