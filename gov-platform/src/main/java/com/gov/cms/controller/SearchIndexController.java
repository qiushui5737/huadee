package com.gov.cms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.SearchIndexConfig;
import com.gov.cms.service.SearchIndexConfigService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cms/search-index")
public class SearchIndexController {
    private final SearchIndexConfigService service;
    public SearchIndexController(SearchIndexConfigService service) { this.service = service; }

    @GetMapping("/list")
    public Result<IPage<SearchIndexConfig>> list(@RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(service.page(page, size));
    }

    @PostMapping("/sync/{indexName}")
    public Result<Void> sync(@PathVariable String indexName) {
        service.sync(indexName);
        return Result.success();
    }

    @PostMapping("/rebuild/{indexName}")
    public Result<Void> rebuild(@PathVariable String indexName) {
        service.fullRebuild(indexName);
        return Result.success();
    }
}


// PATCH: force count fix
