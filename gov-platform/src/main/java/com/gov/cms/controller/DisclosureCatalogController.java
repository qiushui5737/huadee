package com.gov.cms.controller;

import com.gov.cms.entity.DisclosureCatalog;
import com.gov.cms.service.DisclosureCatalogService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cms/disclosure")
public class DisclosureCatalogController {
    private final DisclosureCatalogService service;
    public DisclosureCatalogController(DisclosureCatalogService service) { this.service = service; }

    @GetMapping("/catalog")
    public Result<List<DisclosureCatalog>> catalog(@RequestParam(required = false, defaultValue = "0") Long parentId) {
        return Result.success(service.tree(parentId));
    }

    @PostMapping
    public Result<Void> save(@RequestBody DisclosureCatalog catalog) {
        service.save(catalog);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody DisclosureCatalog catalog) {
        service.update(catalog);
        return Result.success();
    }

    @GetMapping("/catalog/{id}")
    public Result<DisclosureCatalog> getNode(@PathVariable Long id) {
        return Result.success(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }
}
