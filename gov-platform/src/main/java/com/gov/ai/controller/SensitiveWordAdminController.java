package com.gov.ai.controller;

import com.gov.ai.service.SensitiveWordService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/admin/sensitive-words")
public class SensitiveWordAdminController {
    private final SensitiveWordService service;

    public SensitiveWordAdminController(SensitiveWordService service) {
        this.service = service;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() { return Result.success(service.words()); }

    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return Result.success(service.create(body), "敏感词新增成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable long id, @RequestBody Map<String, Object> body) {
        service.update(id, body);
        return Result.success(null, "敏感词更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable long id) {
        service.delete(id);
        return Result.success(null, "敏感词删除成功");
    }
}
