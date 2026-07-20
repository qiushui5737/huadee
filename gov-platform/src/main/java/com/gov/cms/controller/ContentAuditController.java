package com.gov.cms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.cms.entity.ContentAuditRecord;
import com.gov.cms.service.ContentAuditRecordService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cms/audit")
public class ContentAuditController {
    private final ContentAuditRecordService service;
    public ContentAuditController(ContentAuditRecordService service) { this.service = service; }

    @GetMapping("/list")
    public Result<IPage<ContentAuditRecord>> list(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Long contentId) {
        return Result.success(service.page(page, size, contentId));
    }

    @PostMapping("/{contentId}")
    public Result<Void> audit(@PathVariable Long contentId, @RequestBody Map<String,String> body) {
        String action = body.getOrDefault("action", "approve");
        String opinion = body.get("opinion");
        Long operatorId = 1L;
        String operatorName = body.getOrDefault("operatorName", "\u5BA1\u6838\u5458");
        service.audit(contentId, action, opinion, operatorId, operatorName);
        return Result.success();
    }
}
