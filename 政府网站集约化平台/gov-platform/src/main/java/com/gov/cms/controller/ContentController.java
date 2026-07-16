package com.gov.cms.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * D1-部门子站管理 & D2-CMS内容发布
 */
@RestController
@RequestMapping("/api/v1/cms")
public class ContentController {

    @GetMapping("/sites")
    public Result<List<Map<String,Object>>> sites() {
        return Result.success(List.of(
            Map.of("code","EDU","name","教育部"),
            Map.of("code","HEA","name","卫健委"),
            Map.of("code","HOU","name","住建局"),
            Map.of("code","SOC","name","人社局")
        ));
    }

    @GetMapping("/content/list")
    public Result<PageResult<Map<String,Object>>> contentList(
            @RequestParam(required=false) String siteCode,
            @RequestParam(required=false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @PostMapping("/content")
    public Result<Map<String,Object>> publish(@RequestBody Map<String,Object> body) {
        // TODO: 发布内容 -> 自动进入审核流程(D3)
        Map<String,Object> result = new HashMap<>();
        result.put("contentId", System.currentTimeMillis());
        result.put("status", "待审核");
        return Result.success(result, "内容已提交审核");
    }

    @PostMapping("/audit/{contentId}")
    public Result<Void> audit(@PathVariable Long contentId, @RequestBody Map<String,String> body) {
        // TODO: D3-内容审核(初审->复审->终审)
        return Result.success();
    }

    @GetMapping("/disclosure/catalog")
    public Result<List<Map<String,Object>>> disclosureCatalog() {
        // TODO: D4-信息公开目录树
        return Result.success(Collections.emptyList());
    }
}
