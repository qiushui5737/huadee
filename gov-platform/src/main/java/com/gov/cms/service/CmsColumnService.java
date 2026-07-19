package com.gov.cms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.*;
import java.util.List;

public interface CmsColumnService {
    IPage<CmsColumn> page(int page, int size, String siteCode);
    List<CmsColumn> tree(String siteCode);
    boolean save(CmsColumn column);
    boolean update(CmsColumn column);
    boolean delete(Long id);
}
