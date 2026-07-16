package com.gov.ai.controller;

import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * A4-敏感词过滤: DFA算法双向过滤
 */
@RestController
@RequestMapping("/api/v1/ai/sensitive")
public class SensitiveController {

    @PostMapping("/check")
    public Result<Map<String,Object>> check(@RequestBody Map<String,String> body) {
        String text = body.get("text");
        // TODO: DFA算法检测敏感词
        Map<String,Object> result = new HashMap<>();
        result.put("passed", true);
        result.put("filtered", text);
        return Result.success(result);
    }

    @GetMapping("/words")
    public Result<List<String>> listWords() {
        // TODO: 从数据库加载敏感词库
        return Result.success(Collections.emptyList());
    }
}
