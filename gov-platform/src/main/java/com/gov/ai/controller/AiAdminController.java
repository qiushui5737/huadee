package com.gov.ai.controller;

import com.gov.ai.service.AiService;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/admin")
public class AiAdminController {
    private final AiService aiService;
    private final com.gov.ai.service.ElasticsearchSearchService elasticsearchSearchService;

    public AiAdminController(AiService aiService,
            com.gov.ai.service.ElasticsearchSearchService elasticsearchSearchService) {
        this.aiService = aiService;
        this.elasticsearchSearchService = elasticsearchSearchService;
    }

    @GetMapping("/audits")
    public Result<PageResult<Map<String, Object>>> audits(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiService.auditList(keyword, status, page, size));
    }

    @GetMapping("/audits/stats")
    public Result<Map<String, Object>> auditStats() {
        return Result.success(aiService.auditStats());
    }

    @GetMapping("/audits/{id}")
    public Result<Map<String, Object>> auditDetail(@PathVariable long id) {
        return Result.success(aiService.auditDetail(id));
    }

    @PutMapping("/audits/{id}/review")
    public Result<Void> reviewAudit(@PathVariable long id,
            @RequestBody Map<String, String> body,
            @RequestAttribute("userId") Long userId) {
        aiService.reviewAudit(id, userId, body.get("status"), body.get("comment"));
        return Result.success(null, "审计处理完成");
    }

    @GetMapping("/search/status")
    public Result<Map<String, Object>> searchStatus() {
        return Result.success(elasticsearchSearchService.status());
    }

    @PostMapping("/search/rebuild")
    public Result<Map<String, Object>> rebuildSearchIndex() {
        return Result.success(elasticsearchSearchService.rebuild(), "搜索索引重建完成");
    }
}
