package com.gov.ai.controller;

import com.gov.ai.service.AiService;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/search")
public class SearchController {
    private final AiService aiService;

    public SearchController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> search(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiService.search(keyword, page, size));
    }

    @GetMapping("/semantic")
    public Result<PageResult<Map<String, Object>>> semanticSearch(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiService.semanticSearch(keyword, page, size));
    }

    @GetMapping("/hot")
    public Result<List<String>> hotKeywords() {
        return Result.success(aiService.hotKeywords());
    }
}
