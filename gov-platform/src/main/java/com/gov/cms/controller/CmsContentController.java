package com.gov.cms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.CmsContent;
import com.gov.cms.service.CmsContentService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cms/content")
public class CmsContentController {
    private final CmsContentService service;
    public CmsContentController(CmsContentService service) { this.service = service; }

    @GetMapping("/list")
    public Result<IPage<CmsContent>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String siteCode,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(service.page(page, size, siteCode, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<CmsContent> get(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CmsContent content) {
        service.save(content);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CmsContent content) {
        service.update(content);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        service.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/offline")
    public Result<Void> offline(@PathVariable Long id) {
        service.offline(id);
        return Result.success();
    }
}
