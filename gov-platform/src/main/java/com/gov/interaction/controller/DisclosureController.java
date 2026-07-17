package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.DisclosureApply;
import com.gov.interaction.service.DisclosureApplyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C3-依申请公开申请端 & C4-依申请审核
 */
@RestController
@RequestMapping("/api/v1/disclosure")
public class DisclosureController {

    private final DisclosureApplyService disclosureService;

    public DisclosureController(DisclosureApplyService disclosureService) {
        this.disclosureService = disclosureService;
    }

    /**
     * C3-提交公开申请
     */
    @PostMapping("/apply")
    public Result<Map<String, Object>> apply(@RequestBody Map<String, Object> body) {
        DisclosureApply apply = new DisclosureApply();
        apply.setApplicant((String) body.get("applicant"));
        apply.setIdCard((String) body.get("idCard"));
        apply.setPhone((String) body.get("phone"));
        apply.setContent((String) body.get("content"));
        apply.setPurpose((String) body.get("purpose"));
        apply.setAcquireMethod((String) body.get("acquireMethod"));

        DisclosureApply saved = disclosureService.apply(apply);
        return Result.success(Map.of(
                "applyNo", saved.getApplyNo(),
                "status", saved.getStatus(),
                "deadline", saved.getDeadline() != null ? saved.getDeadline().toString() : ""
        ), "申请提交成功");
    }

    /**
     * C3-查询申请进度
     */
    @GetMapping("/progress/{applyNo}")
    public Result<Map<String, Object>> progress(@PathVariable String applyNo) {
        DisclosureApply apply = disclosureService.getByApplyNo(applyNo);
        return Result.success(toMap(apply));
    }

    /**
     * C4-申请列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<DisclosureApply> pageResult = disclosureService.listPage(keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C4-审核申请
     */
    @PostMapping("/audit/{applyNo}")
    public Result<Void> audit(@PathVariable String applyNo, @RequestBody Map<String, String> body) {
        String action = body.get("action");       // approve/reject/processing
        String replyContent = body.get("replyContent");
        String operator = body.getOrDefault("operator", "系统管理员");
        disclosureService.audit(applyNo, action, replyContent, operator);
        return Result.success();
    }

    /**
     * C4-统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("statusStats", disclosureService.statusStats());
        result.put("overdueCount", disclosureService.countOverdue());
        return Result.success(result);
    }

    private Map<String, Object> toMap(DisclosureApply a) {
        return Map.of(
                "id", a.getId() != null ? a.getId() : 0,
                "applyNo", a.getApplyNo() != null ? a.getApplyNo() : "",
                "applicant", a.getApplicant() != null ? a.getApplicant() : "",
                "idCard", a.getIdCard() != null ? a.getIdCard() : "",
                "phone", a.getPhone() != null ? a.getPhone() : "",
                "content", a.getContent() != null ? a.getContent() : "",
                "purpose", a.getPurpose() != null ? a.getPurpose() : "",
                "acquireMethod", a.getAcquireMethod() != null ? a.getAcquireMethod() : "",
                "status", a.getStatus() != null ? a.getStatus() : "",
                "deadline", a.getDeadline() != null ? a.getDeadline().toString() : "",
                "replyContent", a.getReplyContent() != null ? a.getReplyContent() : "",
                "replyBy", a.getReplyBy() != null ? a.getReplyBy() : "",
                "replyTime", a.getReplyTime() != null ? a.getReplyTime().toString() : "",
                "createTime", a.getCreateTime() != null ? a.getCreateTime().toString() : ""
        );
    }
}
package com.gov.interaction.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * C3-依申请公开申请端 & C4-依申请审核
 */
@RestController
@RequestMapping("/api/v1/disclosure")
public class DisclosureController {

    @PostMapping("/apply")
    public Result<Map<String,Object>> apply(@RequestBody Map<String,Object> body) {
        Map<String,Object> result = new HashMap<>();
        result.put("applyNo", "YA2026" + System.currentTimeMillis() % 100000);
        result.put("status", "已受理");
        return Result.success(result, "申请提交成功");
    }

    @GetMapping("/progress/{applyNo}")
    public Result<List<Map<String,Object>>> progress(@PathVariable String applyNo) {
        return Result.success(Collections.emptyList());
    }

    @GetMapping("/list")
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }

    @PostMapping("/audit/{applyNo}")
    public Result<Void> audit(@PathVariable String applyNo, @RequestBody Map<String,String> body) {
        String action = body.get("action");
        return Result.success();
    }
}
