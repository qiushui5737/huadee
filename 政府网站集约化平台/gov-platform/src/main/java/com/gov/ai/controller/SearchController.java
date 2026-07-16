package com.gov.ai.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * A1-智能搜索: ES全文检索
 */
@RestController
@RequestMapping("/api/v1/ai/search")
public class SearchController {

    @GetMapping
    public Result<PageResult<Map<String,Object>>> search(@RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        // TODO: 接入Elasticsearch全文检索
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @GetMapping("/hot")
    public Result<List<String>> hotKeywords() {
        return Result.success(List.of("养老金调整","医保报销","居住证办理","入学政策"));
    }
}
