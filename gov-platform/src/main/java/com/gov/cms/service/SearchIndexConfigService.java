package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;

public interface SearchIndexConfigService {
    IPage<SearchIndexConfig> page(int page, int size);
    boolean sync(String indexName);
    boolean fullRebuild(String indexName);
}
