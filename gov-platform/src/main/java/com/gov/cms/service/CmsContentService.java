package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;

public interface CmsContentService {
    IPage<CmsContent> page(int page, int size, String siteCode, String status, String keyword);
    CmsContent getById(Long id);
    boolean save(CmsContent content);
    boolean update(CmsContent content);
    boolean delete(Long id);
    boolean publish(Long id);
    boolean offline(Long id);
    boolean updateEsSyncStatus(Long id, int status);
}
