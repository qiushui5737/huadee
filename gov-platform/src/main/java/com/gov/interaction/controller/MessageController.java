package com.gov.interaction.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * C1-留言系统 & C2-分派督办
 */
@RestController
@RequestMapping("/api/v1/interaction/message")
public class MessageController {

    @PostMapping
    public Result<Map<String,Object>> submit(@RequestBody Map<String,Object> body) {
        // TODO: 1.敏感词检查 2.保存留言 3.MQ通知分派
        Map<String,Object> result = new HashMap<>();
        result.put("id", System.currentTimeMillis());
        result.put("status", "待分派");
        return Result.success(result, "留言提交成功");
    }

    @GetMapping
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required=false) String keyword,
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 留言列表
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @GetMapping("/hot")
    public Result<List<Map<String,Object>>> hotMessages() {
        // TODO: 热门留言
        return Result.success(Collections.emptyList());
    }

    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String,String> body) {
        // TODO: B端回复留言(MQ通知C端推送)
        return Result.success();
    }
}
