package com.gov.ai.controller;

import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;

/**
 * A3-智能问答: Spring AI流式问答(SSE)
 */
@RestController
@RequestMapping("/api/v1/ai/chat")
public class ChatController {

    /**
     * 流式问答 - SSE推送
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter chatStream(@RequestParam String question,
                                @RequestHeader(value = "Authorization", required = false) String token) {
        SseEmitter emitter = new SseEmitter(120000L);
        // TODO: 1.敏感词校验 2.知识库检索(RAG) 3.Spring AI调用 4.SSE流式推送
        return emitter;
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<Map<String,Object>>> history(@PathVariable String sessionId) {
        // TODO: 从Redis读取会话记忆(最近10轮)
        return Result.success(Collections.emptyList());
    }
}
