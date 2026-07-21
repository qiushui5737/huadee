package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import com.gov.interaction.entity.Suggestion;
import com.gov.interaction.service.SuggestionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 建议接口（C端提交/查询 + B端管理）
 */
@RestController
@RequestMapping("/api/v1/suggestion")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    /**
     * C端-提交建议
     */
    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Suggestion s = new Suggestion();
        // 记录当前用户ID
        try {
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                Long userId = JwtUtil.getUserId(token);
                s.setUserId(userId);
            }
        } catch (Exception ignored) {}

        s.setTitle((String) body.get("title"));
        s.setType((String) body.get("type"));
        s.setRealName((String) body.get("realName"));
        s.setIdCard((String) body.get("idCard"));
        s.setEmail((String) body.get("email"));
        s.setPhone((String) body.get("phone"));
        s.setAddress((String) body.get("address"));
        s.setProvince((String) body.get("province"));
        s.setCity((String) body.get("city"));
        s.setDistrict((String) body.get("district"));
        s.setDetailAddress((String) body.get("detailAddress"));
        s.setContent((String) body.get("content"));
        s.setIsPublic(parseBool(body.get("isPublic"), true));
        s.setIsSecret(parseBool(body.get("isSecret"), false));

        Suggestion saved = suggestionService.submit(s);
        return Result.success(Map.of(
                "suggestNo", saved.getSuggestNo(),
                "status", saved.getStatus(),
                "deadline", saved.getDeadline() != null ? saved.getDeadline().toString() : ""
        ), "建议提交成功");
    }

    /**
     * C端-公开建议列表（分页，无需登录）
     */
    @GetMapping("/public/list")
    public Result<PageResult<Map<String, Object>>> publicList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Suggestion> pageResult = suggestionService.listPage(keyword, status, city, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C端-公开建议详情
     */
    @GetMapping("/public/{id}")
    public Result<Map<String, Object>> publicDetail(@PathVariable Long id) {
        Suggestion s = suggestionService.getById(id);
        if (s == null) {
            return Result.error("建议不存在");
        }
        return Result.success(toMap(s));
    }

    /**
     * C端-根据建议单号查询进度（仅本人）
     */
    @GetMapping("/progress/{suggestNo}")
    public Result<Map<String, Object>> progress(@PathVariable String suggestNo, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId((String) request.getAttribute("jwtToken"));
        Suggestion s = suggestionService.getBySuggestNoAndUserId(suggestNo, userId);
        return Result.success(toMap(s));
    }

    /**
     * B端-建议列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Suggestion> pageResult = suggestionService.listPage(keyword, status, city, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-答复建议
     */
    @PostMapping("/{suggestNo}/reply")
    public Result<Void> reply(@PathVariable String suggestNo, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        String operator = body.getOrDefault("operator", "系统管理员");
        suggestionService.reply(suggestNo, replyContent, operator);
        return Result.success();
    }

    /**
     * B端-修改建议状态
     */
    @PatchMapping("/{suggestNo}/status")
    public Result<Void> updateStatus(@PathVariable String suggestNo, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        suggestionService.updateStatus(suggestNo, status);
        return Result.success();
    }

    /**
     * B端-办结建议
     */
    @PostMapping("/{suggestNo}/finish")
    public Result<Void> finish(@PathVariable String suggestNo) {
        suggestionService.finish(suggestNo);
        return Result.success();
    }

    /**
     * B端-统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("statusStats", suggestionService.statusStats());
        result.put("overdueCount", suggestionService.countOverdue());
        return Result.success(result);
    }

    private Map<String, Object> toMap(Suggestion s) {
        return Map.ofEntries(
                Map.entry("id", s.getId() != null ? s.getId() : 0),
                Map.entry("suggestNo", s.getSuggestNo() != null ? s.getSuggestNo() : ""),
                Map.entry("title", s.getTitle() != null ? s.getTitle() : ""),
                Map.entry("type", s.getType() != null ? s.getType() : ""),
                Map.entry("realName", s.getRealName() != null ? s.getRealName() : ""),
                Map.entry("idCard", s.getIdCard() != null ? s.getIdCard() : ""),
                Map.entry("email", s.getEmail() != null ? s.getEmail() : ""),
                Map.entry("phone", s.getPhone() != null ? s.getPhone() : ""),
                Map.entry("address", s.getAddress() != null ? s.getAddress() : ""),
                Map.entry("province", s.getProvince() != null ? s.getProvince() : ""),
                Map.entry("city", s.getCity() != null ? s.getCity() : ""),
                Map.entry("district", s.getDistrict() != null ? s.getDistrict() : ""),
                Map.entry("detailAddress", s.getDetailAddress() != null ? s.getDetailAddress() : ""),
                Map.entry("content", s.getContent() != null ? s.getContent() : ""),
                Map.entry("isPublic", s.getIsPublic() != null ? s.getIsPublic() : true),
                Map.entry("isSecret", s.getIsSecret() != null ? s.getIsSecret() : false),
                Map.entry("status", s.getStatus() != null ? s.getStatus() : ""),
                Map.entry("replyContent", s.getReplyContent() != null ? s.getReplyContent() : ""),
                Map.entry("replyBy", s.getReplyBy() != null ? s.getReplyBy() : ""),
                Map.entry("replyTime", s.getReplyTime() != null ? s.getReplyTime().toString() : ""),
                Map.entry("deadline", s.getDeadline() != null ? s.getDeadline().toString() : ""),
                Map.entry("createTime", s.getCreateTime() != null ? s.getCreateTime().toString() : "")
        );
    }

    private boolean parseBool(Object val, boolean defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }
}
