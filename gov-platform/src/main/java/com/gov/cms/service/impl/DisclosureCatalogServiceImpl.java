package com.gov.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gov.cms.entity.DisclosureCatalog;
import com.gov.cms.mapper.DisclosureCatalogMapper;
import com.gov.cms.service.DisclosureCatalogService;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.StrUtil;
import java.util.List;

@Service
public class DisclosureCatalogServiceImpl implements DisclosureCatalogService {
    private final DisclosureCatalogMapper mapper;
    public DisclosureCatalogServiceImpl(DisclosureCatalogMapper mapper) { this.mapper = mapper; }

    @Override public List<DisclosureCatalog> tree(Long parentId) {
        LambdaQueryWrapper<DisclosureCatalog> q = new LambdaQueryWrapper<>();
        if (parentId != null && parentId != 0) q.eq(DisclosureCatalog::getParentId, parentId);
        q.orderByAsc(DisclosureCatalog::getSortOrder);
        return mapper.selectList(q);
    }
    @Override public boolean save(DisclosureCatalog catalog) { return mapper.insert(catalog) > 0; }
    @Override public boolean update(DisclosureCatalog catalog) { return mapper.updateById(catalog) > 0; }
    @Override public boolean delete(Long id) { return mapper.deleteById(id) > 0; }
    @Override public DisclosureCatalog getById(Long id) { return mapper.selectById(id); }
}
