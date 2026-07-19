package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import com.gov.interaction.entity.Complaint;
import com.gov.interaction.service.ComplaintService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 投诉接口（C端提交/查询 + B端管理）
 */
@RestController
@RequestMapping("/api/v1/complaint")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * C端-提交投诉
     */
    @PostMapping
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Complaint c = new Complaint();
        try {
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                Long userId = JwtUtil.getUserId(token);
                c.setUserId(userId);
            }
        } catch (Exception ignored) {}

        c.setTitle((String) body.get("title"));
        c.setComplaintUnitL1((String) body.get("complaintUnitL1"));
        c.setComplaintUnitL2((String) body.get("complaintUnitL2"));
        c.setComplaintUnitL3((String) body.get("complaintUnitL3"));
        c.setProblemTypeL1((String) body.get("problemTypeL1"));
        c.setProblemTypeL2((String) body.get("problemTypeL2"));
        c.setProblemTypeL3((String) body.get("problemTypeL3"));
        c.setRealName((String) body.get("realName"));
        c.setIdCard((String) body.get("idCard"));
        c.setEmail((String) body.get("email"));
        c.setPhone((String) body.get("phone"));
        c.setAddress((String) body.get("address"));
        c.setProvince((String) body.get("province"));
        c.setCity((String) body.get("city"));
        c.setDistrict((String) body.get("district"));
        c.setDetailAddress((String) body.get("detailAddress"));
        c.setContent((String) body.get("content"));
        c.setAttachment((String) body.get("attachment"));
        c.setIsPublic(parseBool(body.get("isPublic"), true));
        c.setIsSecret(parseBool(body.get("isSecret"), false));

        Complaint saved = complaintService.submit(c);
        return Result.success(Map.of(
                "complaintNo", saved.getComplaintNo(),
                "status", saved.getStatus(),
                "deadline", saved.getDeadline() != null ? saved.getDeadline().toString() : ""
        ), "投诉提交成功");
    }

    /**
     * C端-根据投诉单号查询进度（仅本人）
     */
    @GetMapping("/progress/{complaintNo}")
    public Result<Map<String, Object>> progress(@PathVariable String complaintNo, HttpServletRequest request) {
        Long userId = JwtUtil.getUserId((String) request.getAttribute("jwtToken"));
        Complaint c = complaintService.getByComplaintNoAndUserId(complaintNo, userId);
        return Result.success(toMap(c));
    }

    /**
     * B端-投诉列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Complaint> pageResult = complaintService.listPage(keyword, status, city, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-答复投诉
     */
    @PostMapping("/{complaintNo}/reply")
    public Result<Void> reply(@PathVariable String complaintNo, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        String operator = body.getOrDefault("operator", "系统管理员");
        complaintService.reply(complaintNo, replyContent, operator);
        return Result.success();
    }

    /**
     * B端-修改投诉状态
     */
    @PatchMapping("/{complaintNo}/status")
    public Result<Void> updateStatus(@PathVariable String complaintNo, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        complaintService.updateStatus(complaintNo, status);
        return Result.success();
    }

    /**
     * B端-办结投诉
     */
    @PostMapping("/{complaintNo}/finish")
    public Result<Void> finish(@PathVariable String complaintNo) {
        complaintService.finish(complaintNo);
        return Result.success();
    }

    /**
     * B端-统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();
        result.put("statusStats", complaintService.statusStats());
        result.put("overdueCount", complaintService.countOverdue());
        return Result.success(result);
    }

    private Map<String, Object> toMap(Complaint c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId() != null ? c.getId() : 0);
        map.put("complaintNo", c.getComplaintNo() != null ? c.getComplaintNo() : "");
        map.put("title", c.getTitle() != null ? c.getTitle() : "");
        map.put("complaintUnitL1", c.getComplaintUnitL1() != null ? c.getComplaintUnitL1() : "");
        map.put("complaintUnitL2", c.getComplaintUnitL2() != null ? c.getComplaintUnitL2() : "");
        map.put("complaintUnitL3", c.getComplaintUnitL3() != null ? c.getComplaintUnitL3() : "");
        map.put("problemTypeL1", c.getProblemTypeL1() != null ? c.getProblemTypeL1() : "");
        map.put("problemTypeL2", c.getProblemTypeL2() != null ? c.getProblemTypeL2() : "");
        map.put("problemTypeL3", c.getProblemTypeL3() != null ? c.getProblemTypeL3() : "");
        map.put("realName", c.getRealName() != null ? c.getRealName() : "");
        map.put("idCard", c.getIdCard() != null ? c.getIdCard() : "");
        map.put("email", c.getEmail() != null ? c.getEmail() : "");
        map.put("phone", c.getPhone() != null ? c.getPhone() : "");
        map.put("address", c.getAddress() != null ? c.getAddress() : "");
        map.put("province", c.getProvince() != null ? c.getProvince() : "");
        map.put("city", c.getCity() != null ? c.getCity() : "");
        map.put("district", c.getDistrict() != null ? c.getDistrict() : "");
        map.put("detailAddress", c.getDetailAddress() != null ? c.getDetailAddress() : "");
        map.put("content", c.getContent() != null ? c.getContent() : "");
        map.put("attachment", c.getAttachment() != null ? c.getAttachment() : "");
        map.put("isPublic", c.getIsPublic() != null ? c.getIsPublic() : true);
        map.put("isSecret", c.getIsSecret() != null ? c.getIsSecret() : false);
        map.put("status", c.getStatus() != null ? c.getStatus() : "");
        map.put("replyContent", c.getReplyContent() != null ? c.getReplyContent() : "");
        map.put("replyBy", c.getReplyBy() != null ? c.getReplyBy() : "");
        map.put("replyTime", c.getReplyTime() != null ? c.getReplyTime().toString() : "");
        map.put("deadline", c.getDeadline() != null ? c.getDeadline().toString() : "");
        map.put("createTime", c.getCreateTime() != null ? c.getCreateTime().toString() : "");
        return map;
    }

    private boolean parseBool(Object val, boolean defaultVal) {
        if (val == null) return defaultVal;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }
}
