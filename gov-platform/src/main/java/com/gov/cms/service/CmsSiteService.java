package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;

public interface CmsSiteService {
    IPage<CmsSite> page(int page, int size, String keyword);
    CmsSite getBySiteCode(String siteCode);
    boolean save(CmsSite site);
    boolean update(CmsSite site);
    boolean delete(Long id);
}
