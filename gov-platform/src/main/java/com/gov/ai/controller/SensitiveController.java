package com.gov.ai.controller;

import com.gov.ai.service.AiService;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai/sensitive")
public class SensitiveController {
    private final AiService aiService;

    public SensitiveController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/check")
    public Result<Map<String, Object>> check(@RequestBody Map<String, String> body) {
        return Result.success(aiService.checkSensitive(body.get("text")));
    }

    @GetMapping("/words")
    public Result<List<Map<String, Object>>> listWords() {
        return Result.success(aiService.sensitiveWords());
    }
}
