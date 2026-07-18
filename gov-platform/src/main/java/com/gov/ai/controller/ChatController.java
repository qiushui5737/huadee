package com.gov.ai.controller;

import com.gov.ai.service.AiService;
import com.gov.admin.service.TokenSessionService;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/ai/chat")
public class ChatController {
    private final AiService aiService;
    private final TokenSessionService tokenSessionService;
    private final Executor aiTaskExecutor;

    public ChatController(AiService aiService,
            TokenSessionService tokenSessionService,
            @Qualifier("aiTaskExecutor") Executor aiTaskExecutor) {
        this.aiService = aiService;
        this.tokenSessionService = tokenSessionService;
        this.aiTaskExecutor = aiTaskExecutor;
    }

    /** Compatibility endpoint for existing clients. New clients should use POST /stream. */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter chatStream(@RequestParam String question,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return createStream(question, sessionId, resolveOptionalUserId(token));
    }

    @PostMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter chatStreamPost(@RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return createStream(body.get("question"), body.get("sessionId"), resolveOptionalUserId(token));
    }

    private SseEmitter createStream(String question, String sessionId, Long userId) {
        SseEmitter emitter = new SseEmitter(180000L);
        aiTaskExecutor.execute(() -> {
            try {
                aiService.streamAnswer(
                    question,
                    sessionId,
                    userId,
                    meta -> sendEvent(emitter, "meta", meta),
                    chunk -> sendEvent(emitter, "chunk", chunk)
                );
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("智能问答服务暂时不可用，请稍后重试"));
                } catch (IOException ignored) {
                }
                emitter.complete();
            }
        });
        return emitter;
    }

    @PostMapping
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return Result.success(aiService.answer(
                body.get("question"),
                body.get("sessionId"),
                resolveOptionalUserId(token)
        ));
    }

    @GetMapping("/history/{sessionId}")
    public Result<List<Map<String, Object>>> history(@PathVariable String sessionId,
            @RequestAttribute("userId") Long userId) {
        return Result.success(aiService.history(sessionId, userId));
    }

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> conversations(@RequestAttribute("userId") Long userId) {
        return Result.success(aiService.conversations(userId));
    }

    @PostMapping("/conversations")
    public Result<Map<String, Object>> createConversation(
            @RequestBody(required = false) Map<String, String> body,
            @RequestAttribute("userId") Long userId) {
        return Result.success(aiService.createConversation(userId, body == null ? null : body.get("title")), "会话创建成功");
    }

    @PutMapping("/conversations/{sessionId}")
    public Result<Void> renameConversation(@PathVariable String sessionId,
            @RequestBody Map<String, String> body,
            @RequestAttribute("userId") Long userId) {
        aiService.renameConversation(sessionId, userId, body.get("title"));
        return Result.success(null, "会话重命名成功");
    }

    @DeleteMapping("/conversations/{sessionId}")
    public Result<Void> deleteConversation(@PathVariable String sessionId,
            @RequestAttribute("userId") Long userId) {
        aiService.deleteConversation(sessionId, userId);
        return Result.success(null, "会话删除成功");
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

    private Long resolveOptionalUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring(7);
        if (!JwtUtil.isValid(token) || !tokenSessionService.isValid(token)) {
            return null;
        }
        return JwtUtil.getUserId(token);
    }
}
