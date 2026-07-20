package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;
import java.util.List;

public interface DisclosureCatalogService {
    List<DisclosureCatalog> tree(Long parentId);
    boolean save(DisclosureCatalog catalog);
    boolean update(DisclosureCatalog catalog);
    boolean delete(Long id);
    DisclosureCatalog getById(Long id);
}
