package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gov.cms.entity.CmsColumn;
import com.gov.cms.mapper.CmsColumnMapper;
import com.gov.cms.service.CmsColumnService;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;
import java.util.List;

@Service
public class CmsColumnServiceImpl implements CmsColumnService {
    private final CmsColumnMapper mapper;
    public CmsColumnServiceImpl(CmsColumnMapper mapper) { this.mapper = mapper; }

    @Override public IPage<CmsColumn> page(int page, int size, String siteCode) {
        LambdaQueryWrapper<CmsColumn> q = new LambdaQueryWrapper<>();
        q.eq(CmsColumn::getDeleted, 0);
        if (StrUtil.isNotBlank(siteCode)) q.eq(CmsColumn::getSiteCode, siteCode);
        q.orderByAsc(CmsColumn::getSortOrder);
        return mapper.selectPage(new Page<>(page, size), q);
    }
    @Override public List<CmsColumn> tree(String siteCode) {
        LambdaQueryWrapper<CmsColumn> q = new LambdaQueryWrapper<>();
        q.eq(CmsColumn::getDeleted, 0);
        if (StrUtil.isNotBlank(siteCode)) q.eq(CmsColumn::getSiteCode, siteCode);
        q.orderByAsc(CmsColumn::getParentId).orderByAsc(CmsColumn::getSortOrder);
        return mapper.selectList(q);
    }
    @Override public boolean save(CmsColumn column) { return mapper.insert(column) > 0; }
    @Override public boolean update(CmsColumn column) { return mapper.updateById(column) > 0; }
    @Override public boolean delete(Long id) { return mapper.deleteById(id) > 0; }
}
