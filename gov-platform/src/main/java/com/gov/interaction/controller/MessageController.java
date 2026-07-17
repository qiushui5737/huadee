package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.Message;
import com.gov.interaction.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C1-留言系统 & C2-分派督办
 */
@RestController
@RequestMapping("/api/v1/interaction/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * C1-提交留言（C端群众）
     */
    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Message message = new Message();
        message.setTitle((String) body.get("title"));
        message.setContent((String) body.get("content"));
        message.setUserName((String) body.get("contactName"));
        message.setDeptCode((String) body.get("targetDept"));
        message.setType((String) body.get("type"));

        Message saved = messageService.submit(message);

        Map<String, Object> result = Map.of(
                "id", saved.getId(),
                "status", saved.getStatus()
        );
        return Result.success(result, "留言提交成功");
    }

    /**
     * C1-留言列表（分页）
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Message> pageResult = messageService.listPage(keyword, status, type, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C1-留言详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(toMap(messageService.detail(id)));
    }

    /**
     * C1-热门留言
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hotMessages() {
        List<Map<String, Object>> records = messageService.hotMessages().stream()
                .map(this::toMap)
                .toList();
        return Result.success(records);
    }

    /**
     * C1-评价留言
     */
    @PostMapping("/{id}/rate")
    public Result<Void> rate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer rating = (Integer) body.get("rating");
        String content = (String) body.get("content");
        messageService.rate(id, rating, content);
        return Result.success();
    }

    /**
     * C2-B端回复留言
     */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        String operator = body.getOrDefault("operator", "系统管理员");
        messageService.reply(id, content, operator);
        return Result.success();
    }

    /**
     * C2-B端分派留言到部门
     */
    @PostMapping("/{id}/dispatch")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String deptCode = body.get("deptCode");
        messageService.dispatch(id, deptCode);
        return Result.success();
    }

    /**
     * C2-B端办结留言
     */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        messageService.finish(id);
        return Result.success();
    }

    /**
     * C2-B端督办留言
     */
    @PostMapping("/{id}/supervise")
    public Result<Void> supervise(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String operator = body.getOrDefault("operator", "系统管理员");
        messageService.supervise(id, operator);
        return Result.success();
    }

    /**
     * C2-B端统计（各状态数量 + 超时数量）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("statusStats", messageService.statusStats());
        result.put("overdueCount", messageService.countOverdue());
        return Result.success(result);
    }

    /**
     * 实体转Map
     */
    private Map<String, Object> toMap(Message m) {
        return Map.of(
                "id", m.getId() != null ? m.getId() : 0,
                "title", m.getTitle() != null ? m.getTitle() : "",
                "content", m.getContent() != null ? m.getContent() : "",
                "type", m.getType() != null ? m.getType() : "咨询",
                "userName", m.getUserName() != null ? m.getUserName() : "",
                "deptCode", m.getDeptCode() != null ? m.getDeptCode() : "",
                "status", m.getStatus() != null ? m.getStatus() : "",
                "supervise", m.getSupervise() != null ? m.getSupervise() : false,
                "deadline", m.getDeadline() != null ? m.getDeadline().toString() : "",
                "replyContent", m.getReplyContent() != null ? m.getReplyContent() : "",
                "replyBy", m.getReplyBy() != null ? m.getReplyBy() : "",
                "replyTime", m.getReplyTime() != null ? m.getReplyTime().toString() : "",
                "rating", m.getRating() != null ? m.getRating() : 0,
                "ratingContent", m.getRatingContent() != null ? m.getRatingContent() : "",
                "createTime", m.getCreateTime() != null ? m.getCreateTime().toString() : ""
        );
    }
}
package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.Message;
import com.gov.interaction.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C1-留言系统 & C2-分派督办
 */
@RestController
@RequestMapping("/api/v1/interaction/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * C1-提交留言（C端群众）
     */
    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Message message = new Message();
        message.setTitle((String) body.get("title"));
        message.setContent((String) body.get("content"));
        message.setUserName((String) body.get("contactName"));
        message.setDeptCode((String) body.get("targetDept"));

        Message saved = messageService.submit(message);

        Map<String, Object> result = Map.of(
                "id", saved.getId(),
                "status", saved.getStatus()
        );
        return Result.success(result, "留言提交成功");
    }

    /**
     * C1-留言列表（分页）
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Message> pageResult = messageService.listPage(keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C1-热门留言
     */
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> hotMessages() {
        List<Map<String, Object>> records = messageService.hotMessages().stream()
                .map(this::toMap)
                .toList();
        return Result.success(records);
    }

    /**
     * C2-B端回复留言
     */
    @PostMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        String operator = body.getOrDefault("operator", "系统管理员");
        messageService.reply(id, content, operator);
        return Result.success();
    }

    /**
     * C2-B端分派留言到部门
     */
    @PostMapping("/{id}/dispatch")
    public Result<Void> dispatch(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String deptCode = body.get("deptCode");
        messageService.dispatch(id, deptCode);
        return Result.success();
    }

    /**
     * C2-B端办结留言
     */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        messageService.finish(id);
        return Result.success();
    }

    /**
     * 实体转Map
     */
    private Map<String, Object> toMap(Message m) {
        return Map.of(
                "id", m.getId() != null ? m.getId() : 0,
                "title", m.getTitle() != null ? m.getTitle() : "",
                "content", m.getContent() != null ? m.getContent() : "",
                "userName", m.getUserName() != null ? m.getUserName() : "",
                "deptCode", m.getDeptCode() != null ? m.getDeptCode() : "",
                "status", m.getStatus() != null ? m.getStatus() : "",
                "replyContent", m.getReplyContent() != null ? m.getReplyContent() : "",
                "replyBy", m.getReplyBy() != null ? m.getReplyBy() : "",
                "replyTime", m.getReplyTime() != null ? m.getReplyTime().toString() : "",
                "createTime", m.getCreateTime() != null ? m.getCreateTime().toString() : ""
        );
    }
}
