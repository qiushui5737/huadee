package com.gov.ai.controller;

import com.gov.ai.service.AiService;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/ai/chat")
public class ChatController {
    private final AiService aiService;

    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter chatStream(@RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        SseEmitter emitter = new SseEmitter(180000L);
        new Thread(() -> {
            try {
                aiService.streamAnswer(
                    question,
                    sessionId,
                    meta -> sendEvent(emitter, "meta", meta),
                    chunk -> sendEvent(emitter, "chunk", chunk)
                );
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (IOException ignored) {
                }
                emitter.complete();
            }
        }, "ai-chat-stream").start();
        return emitter;
    }

    @PostMapping
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        return Result.success(aiService.answer(body.get("question"), body.get("sessionId")));
    }

    @GetMapping("/history/{sessionId}")
    public Result<List<Map<String, Object>>> history(@PathVariable String sessionId) {
        return Result.success(aiService.history(sessionId));
    }

    @GetMapping("/audit")
    public Result<PageResult<Map<String, Object>>> audit(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(aiService.auditList(keyword, status, page, size));
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
