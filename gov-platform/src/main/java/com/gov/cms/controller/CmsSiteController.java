package com.gov.cms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.CmsSite;
import com.gov.cms.service.CmsSiteService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cms/site")
public class CmsSiteController {
    private final CmsSiteService service;
    public CmsSiteController(CmsSiteService service) { this.service = service; }

    @GetMapping("/list")
    public Result<IPage<CmsSite>> list(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam(required = false) String keyword) {
        return Result.success(service.page(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<CmsSite> get(@PathVariable Long id) {
        CmsSite s = new CmsSite();
        s.setId(id);
        return Result.success(s);
    }

    @GetMapping("/by-code/{siteCode}")
    public Result<CmsSite> getByCode(@PathVariable String siteCode) {
        return Result.success(service.getBySiteCode(siteCode));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CmsSite site) {
        service.save(site);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CmsSite site) {
        service.update(site);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}
