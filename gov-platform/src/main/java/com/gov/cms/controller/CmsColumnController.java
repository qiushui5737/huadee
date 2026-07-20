package com.gov.cms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.CmsColumn;
import com.gov.cms.service.CmsColumnService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cms/column")
public class CmsColumnController {
    private final CmsColumnService service;
    public CmsColumnController(CmsColumnService service) { this.service = service; }

    @GetMapping("/list")
    public Result<IPage<CmsColumn>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String siteCode) {
        return Result.success(service.page(page, size, siteCode));
    }

    @GetMapping("/tree")
    public Result<List<CmsColumn>> tree(@RequestParam(required = false) String siteCode) {
        return Result.success(service.tree(siteCode));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CmsColumn column) {
        service.save(column);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CmsColumn column) {
        service.update(column);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}
