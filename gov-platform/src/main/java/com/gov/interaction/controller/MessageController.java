
package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import com.gov.interaction.entity.Message;
import com.gov.interaction.entity.MessageReply;
import com.gov.interaction.service.MessageService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Message message = new Message();
        message.setTitle((String) body.get("title"));
        message.setContent((String) body.get("content"));
        message.setUserName((String) body.get("contactName"));
        message.setDeptCode((String) body.get("targetDept"));
        message.setType((String) body.get("type"));
        // 是否公开
        Object isPublicObj = body.get("isPublic");
        if (isPublicObj instanceof Boolean) {
            message.setIsPublic((Boolean) isPublicObj);
        } else {
            message.setIsPublic(true);
        }
        // 记录当前用户ID
        try {
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                Long userId = JwtUtil.getUserId(token);
                message.setUserId(userId);
            }
        } catch (Exception ignored) {}

        Message saved = messageService.submit(message);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        result.put("consultNo", saved.getConsultNo());
        return Result.success(result, "留言提交成功");
    }

    /**
     * C1-留言列表（分页，只显示公开的+自己的）
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String consultNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        boolean isAdmin = false;
        Long currentUserId = null;
        try {
            if (token != null) {
                Claims claims = JwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                isAdmin = "ADMIN".equals(role);
                if (!isAdmin) {
                    currentUserId = JwtUtil.getUserId(token);
                }
            }
        } catch (Exception ignored) {}
        IPage<Message> pageResult;
        if (isAdmin) {
            pageResult = messageService.listPageAdmin(keyword, status, type, page, size);
        } else {
            pageResult = messageService.listPage(keyword, consultNo, status, type, currentUserId, isPublic, page, size);
        }
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C1-留言详情（包含对话记录，校验可见性）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id, HttpServletRequest request) {
        String token = (String) request.getAttribute("jwtToken");
        boolean isAdmin = false;
        Long currentUserId = null;
        try {
            if (token != null) {
                Claims claims = JwtUtil.parseToken(token);
                String role = claims.get("role", String.class);
                isAdmin = "ADMIN".equals(role);
                if (!isAdmin) {
                    currentUserId = JwtUtil.getUserId(token);
                }
            }
        } catch (Exception ignored) {}
        Map<String, Object> data;
        if (isAdmin) {
            data = new java.util.HashMap<>(toMap(messageService.detail(id)));
        } else {
            data = new java.util.HashMap<>(toMap(messageService.detail(id, currentUserId)));
        }
        // 添加对话记录
        List<MessageReply> replies = messageService.listReplies(id);
        List<Map<String, Object>> replyList = replies.stream()
                .map(this::replyToMap)
                .collect(Collectors.toList());
        data.put("replies", replyList);
        return Result.success(data);
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
     * C1-用户回复留言（C端群众对管理端回复进行应答，仅信件本人可回复）
     */
    @PostMapping("/{id}/user-reply")
    public Result<Void> userReply(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        // 从JWT获取当前用户ID，而非请求体
        String token = (String) request.getAttribute("jwtToken");
        Long userId = null;
        String userName = null;
        if (token != null) {
            userId = JwtUtil.getUserId(token);
            Claims claims = JwtUtil.parseToken(token);
            userName = claims.get("username", String.class);
        }
        if (userId == null) {
            return Result.error(403, "请先登录");
        }
        if (userName == null || userName.isEmpty()) {
            userName = (String) body.get("userName");
        }
        String content = (String) body.get("content");
        messageService.userReply(id, userId, userName, content);
        return Result.success();
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
        return Map.ofEntries(
                Map.entry("id", m.getId() != null ? m.getId() : 0),
                Map.entry("title", m.getTitle() != null ? m.getTitle() : ""),
                Map.entry("content", m.getContent() != null ? m.getContent() : ""),
                Map.entry("type", m.getType() != null ? m.getType() : "咨询"),
                Map.entry("userName", m.getUserName() != null ? m.getUserName() : ""),
                Map.entry("deptCode", m.getDeptCode() != null ? m.getDeptCode() : ""),
                Map.entry("status", m.getStatus() != null ? m.getStatus() : ""),
                Map.entry("supervise", m.getSupervise() != null ? m.getSupervise() : false),
                Map.entry("deadline", m.getDeadline() != null ? m.getDeadline().toString() : ""),
                Map.entry("replyContent", m.getReplyContent() != null ? m.getReplyContent() : ""),
                Map.entry("replyBy", m.getReplyBy() != null ? m.getReplyBy() : ""),
                Map.entry("replyTime", m.getReplyTime() != null ? m.getReplyTime().toString() : ""),
                Map.entry("rating", m.getRating() != null ? m.getRating() : 0),
                Map.entry("ratingContent", m.getRatingContent() != null ? m.getRatingContent() : ""),
                Map.entry("createTime", m.getCreateTime() != null ? m.getCreateTime().toString() : ""),
                Map.entry("isPublic", m.getIsPublic() != null ? m.getIsPublic() : true),
                Map.entry("consultNo", m.getConsultNo() != null ? m.getConsultNo() : ""),
                Map.entry("userId", m.getUserId() != null ? m.getUserId() : 0)
        );
    }

    /**
     * 回复记录转Map
     */
    private Map<String, Object> replyToMap(MessageReply r) {
        return Map.ofEntries(
                Map.entry("id", r.getId() != null ? r.getId() : 0),
                Map.entry("messageId", r.getMessageId() != null ? r.getMessageId() : 0),
                Map.entry("userId", r.getUserId() != null ? r.getUserId() : 0),
                Map.entry("userName", r.getUserName() != null ? r.getUserName() : ""),
                Map.entry("userType", r.getUserType() != null ? r.getUserType() : "ADMIN"),
                Map.entry("content", r.getContent() != null ? r.getContent() : ""),
                Map.entry("createTime", r.getCreateTime() != null ? r.getCreateTime().toString() : "")
        );
    }
}
