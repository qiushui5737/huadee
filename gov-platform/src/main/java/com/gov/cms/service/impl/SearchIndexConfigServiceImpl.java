package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.cms.entity.SearchIndexConfig;
import com.gov.cms.mapper.SearchIndexConfigMapper;
import com.gov.cms.service.SearchIndexConfigService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.gov.cms.mapper.CmsContentMapper;
import com.gov.cms.entity.CmsContent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Service
public class SearchIndexConfigServiceImpl implements SearchIndexConfigService {
    private final SearchIndexConfigMapper mapper;
    public SearchIndexConfigServiceImpl(SearchIndexConfigMapper mapper) { this.mapper = mapper; }

    @Override public IPage<SearchIndexConfig> page(int page, int size) {
        return mapper.selectPage(new Page<>(page, size), null);
    }
    @Override public boolean sync(String indexName) {
        SearchIndexConfig config = mapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchIndexConfig>()
                .eq(SearchIndexConfig::getIndexName, indexName));
        if (config == null) return false;
        config.setLastSyncAt(LocalDateTime.now());
        int cnt = contentMapper.selectCount(null);
        config.setDocCount(cnt);
        return mapper.updateById(config) > 0;
    }
    @Override public boolean fullRebuild(String indexName) {
        SearchIndexConfig config = mapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SearchIndexConfig>()
                .eq(SearchIndexConfig::getIndexName, indexName));
        if (config == null) return false;
        config.setLastSyncAt(LocalDateTime.now());
                int cnt = contentMapper.selectCount(null);
        config.setDocCount(cnt);
        return mapper.updateById(config) > 0;
    }
