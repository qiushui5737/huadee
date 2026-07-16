package com.gov.service.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * B1-办事事项目录管理
 */
@RestController
@RequestMapping("/api/v1/service/catalog")
public class CatalogController {

    @GetMapping("/categories")
    public Result<List<Map<String,Object>>> categories() {
        return Result.success(List.of(
            Map.of("code","CAT01","name","户籍办理"),
            Map.of("code","CAT02","name","社保服务"),
            Map.of("code","CAT03","name","医保服务"),
            Map.of("code","CAT04","name","教育服务"),
            Map.of("code","CAT05","name","住房服务"),
            Map.of("code","CAT06","name","就业创业")
        ));
    }

    @GetMapping("/items")
    public Result<PageResult<Map<String,Object>>> items(@RequestParam(required=false) String category,
            @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        // TODO: 事项列表查询
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @GetMapping("/items/{id}")
    public Result<Map<String,Object>> itemDetail(@PathVariable Long id) {
        // TODO: 事项详情
        return Result.success(Collections.emptyMap());
    }
}
