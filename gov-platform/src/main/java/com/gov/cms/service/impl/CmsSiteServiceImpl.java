package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.cms.entity.CmsSite;
import com.gov.cms.entity.CmsColumn;
import com.gov.cms.mapper.CmsSiteMapper;
import com.gov.cms.mapper.CmsColumnMapper;
import com.gov.cms.service.CmsSiteService;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;

@Service
public class CmsSiteServiceImpl implements CmsSiteService {
    private final CmsSiteMapper mapper;
    private final CmsColumnMapper columnMapper;
    public CmsSiteServiceImpl(CmsSiteMapper mapper, CmsColumnMapper columnMapper) {
        this.mapper = mapper;
        this.columnMapper = columnMapper;
    }

    @Override public IPage<CmsSite> page(int page, int size, String keyword) {
        LambdaQueryWrapper<CmsSite> q = new LambdaQueryWrapper<>();
        q.eq(CmsSite::getDeleted, 0);
        if (StrUtil.isNotBlank(keyword))
            q.like(CmsSite::getSiteName, keyword).or().like(CmsSite::getSiteCode, keyword);
        q.orderByAsc(CmsSite::getSortOrder);
        return mapper.selectPage(new Page<>(page, size), q);
    }
    @Override public CmsSite getBySiteCode(String siteCode) {
        return mapper.selectOne(new LambdaQueryWrapper<CmsSite>().eq(CmsSite::getSiteCode, siteCode));
    }
    @Override public boolean save(CmsSite site) { return mapper.insert(site) > 0; }
    @Override public boolean update(CmsSite site) { return mapper.updateById(site) > 0; }
    @Override public boolean delete(Long id) {
        // 级联删除: 先删该子站下的所有栏目
        CmsSite site = mapper.selectById(id);
        if (site != null && site.getSiteCode() != null) {
            columnMapper.delete(new LambdaQueryWrapper<CmsColumn>()
                .eq(CmsColumn::getSiteCode, site.getSiteCode()));
        }
        return mapper.deleteById(id) > 0;
    }
}