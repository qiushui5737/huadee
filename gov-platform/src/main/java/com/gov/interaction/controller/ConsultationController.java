package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.Consultation;
import com.gov.interaction.service.ConsultationService;
import com.gov.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 我要咨询 - 咨询接口（C端提交/查询 + B端管理）
 */
@RestController
@RequestMapping("/api/v1/consultation")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /**
     * C端-提交咨询
     */
    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId((String) request.getAttribute("jwtToken"));
        Consultation c = new Consultation();
        c.setUserId(userId);
        c.setRealName((String) body.get("realName"));
        c.setPhone((String) body.get("phone"));
        c.setTelephone((String) body.get("telephone"));
        c.setEmail((String) body.get("email"));
        c.setTitle((String) body.get("title"));
        c.setCity((String) body.get("city"));
        c.setDistrict((String) body.get("district"));
        c.setStreet((String) body.get("street"));
        c.setAddress((String) body.get("address"));
        c.setAttachment((String) body.get("attachment"));
        c.setContent((String) body.get("content"));
        c.setIsPublic(parseBool(body.get("isPublic"), true));
        c.setIsConfidential(parseBool(body.get("isConfidential"), false));

        Consultation saved = consultationService.submit(c);
        return Result.success(Map.of(
                "consultNo", saved.getConsultNo(),
                "status", saved.getStatus(),
                "deadline", saved.getDeadline() != null ? saved.getDeadline().toString() : ""
        ), "咨询提交成功");
    }

    /**
     * C端-根据咨询单号查询进度（仅本人）
     */
    @GetMapping("/progress/{consultNo}")
    public Result<Map<String, Object>> progress(@PathVariable String consultNo, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId((String) request.getAttribute("jwtToken"));
        Consultation c = consultationService.getByConsultNoAndUserId(consultNo, userId);
        return Result.success(toMap(c));
    }

    /**
     * B端-咨询列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Consultation> pageResult = consultationService.listPage(keyword, status, city, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-答复咨询
     */
    @PostMapping("/{consultNo}/reply")
    public Result<Void> reply(@PathVariable String consultNo, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        String operator = body.getOrDefault("operator", "系统管理员");
        consultationService.reply(consultNo, replyContent, operator);
        return Result.success();
    }

    /**
     * B端-修改咨询状态
     */
    @PatchMapping("/{consultNo}/status")
    public Result<Void> updateStatus(@PathVariable String consultNo, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        consultationService.updateStatus(consultNo, status);
        return Result.success();
    }

    /**
     * B端-办结咨询
     */
    @PostMapping("/{consultNo}/finish")
    public Result<Void> finish(@PathVariable String consultNo) {
        consultationService.finish(consultNo);
        return Result.success();
    }

    /**
     * B端-统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("statusStats", consultationService.statusStats());
        result.put("overdueCount", consultationService.countOverdue());
        return Result.success(result);
    }

    private Map<String, Object> toMap(Consultation c) {
        return Map.ofEntries(
                Map.entry("id", c.getId() != null ? c.getId() : 0),
                Map.entry("consultNo", c.getConsultNo() != null ? c.getConsultNo() : ""),
                Map.entry("realName", c.getRealName() != null ? c.getRealName() : ""),
                Map.entry("phone", c.getPhone() != null ? c.getPhone() : ""),
                Map.entry("telephone", c.getTelephone() != null ? c.getTelephone() : ""),
                Map.entry("email", c.getEmail() != null ? c.getEmail() : ""),
                Map.entry("title", c.getTitle() != null ? c.getTitle() : ""),
                Map.entry("city", c.getCity() != null ? c.getCity() : ""),
                Map.entry("district", c.getDistrict() != null ? c.getDistrict() : ""),
                Map.entry("street", c.getStreet() != null ? c.getStreet() : ""),
                Map.entry("address", c.getAddress() != null ? c.getAddress() : ""),
                Map.entry("content", c.getContent() != null ? c.getContent() : ""),
                Map.entry("isPublic", c.getIsPublic() != null ? c.getIsPublic() : true),
                Map.entry("isConfidential", c.getIsConfidential() != null ? c.getIsConfidential() : false),
                Map.entry("attachment", c.getAttachment() != null ? c.getAttachment() : ""),
                Map.entry("status", c.getStatus() != null ? c.getStatus() : ""),
                Map.entry("replyContent", c.getReplyContent() != null ? c.getReplyContent() : ""),
                Map.entry("replyBy", c.getReplyBy() != null ? c.getReplyBy() : ""),
                Map.entry("replyTime", c.getReplyTime() != null ? c.getReplyTime().toString() : ""),
                Map.entry("deadline", c.getDeadline() != null ? c.getDeadline().toString() : ""),
                Map.entry("createTime", c.getCreateTime() != null ? c.getCreateTime().toString() : "")
        );
    }

    private boolean parseBool(Object val, boolean defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }
}
