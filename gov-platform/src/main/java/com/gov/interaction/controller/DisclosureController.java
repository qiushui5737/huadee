package com.gov.interaction.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * C3-依申请公开申请端 & C4-依申请审核
 */
@RestController
@RequestMapping("/api/v1/disclosure")
public class DisclosureController {

    @PostMapping("/apply")
    public Result<Map<String,Object>> apply(@RequestBody Map<String,Object> body) {
        Map<String,Object> result = new HashMap<>();
        result.put("applyNo", "YA2026" + System.currentTimeMillis() % 100000);
        result.put("status", "已受理");
        return Result.success(result, "申请提交成功");
    }

    @GetMapping("/progress/{applyNo}")
    public Result<List<Map<String,Object>>> progress(@PathVariable String applyNo) {
        return Result.success(Collections.emptyList());
    }

    @GetMapping("/list")
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @PostMapping("/audit/{applyNo}")
    public Result<Void> audit(@PathVariable String applyNo, @RequestBody Map<String,String> body) {
        String action = body.get("action");
        return Result.success();
    }
}
