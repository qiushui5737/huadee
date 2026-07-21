
package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.DisclosureApply;
import com.gov.interaction.entity.DisclosureAuditRecord;
import com.gov.interaction.entity.DisclosureFile;
import com.gov.interaction.entity.DisclosureFileAccess;
import com.gov.interaction.service.DisclosureApplyService;
import com.gov.interaction.service.DisclosureFileService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C3-依申请公开申请端 & C4-依申请审核
 */
@RestController
@RequestMapping("/api/v1/disclosure")
public class DisclosureController {

    private final DisclosureApplyService disclosureService;
    private final DisclosureFileService disclosureFileService;

    public DisclosureController(DisclosureApplyService disclosureService, DisclosureFileService disclosureFileService) {
        this.disclosureService = disclosureService;
        this.disclosureFileService = disclosureFileService;
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
        // 关联文件ID
        Object fileIdObj = body.get("fileId");
        if (fileIdObj != null) {
            apply.setFileId(Long.valueOf(fileIdObj.toString()));
        }

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
     * C4-直接修改申请状态
     */
    @PatchMapping("/status/{applyNo}")
    public Result<Void> updateStatus(@PathVariable String applyNo, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        String operator = body.getOrDefault("operator", "系统管理员");
        disclosureService.updateStatus(applyNo, newStatus, operator);
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
    
    /**
     * C端-获取审批流程记录
     */
    @GetMapping("/audit-records/{applyNo}")
    public Result<List<Map<String, Object>>> auditRecords(@PathVariable String applyNo) {
        List<DisclosureAuditRecord> records = disclosureService.getAuditRecords(applyNo);
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("applyNo", r.getApplyNo());
            map.put("nodeName", r.getNodeName());
            map.put("action", r.getAction());
            map.put("opinion", r.getOpinion());
            map.put("operatorName", r.getOperatorName());
            map.put("operateTime", r.getOperateTime() != null ? r.getOperateTime().toString() : "");
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
    
    /**
     * C端-获取授权文件下载链接（带权限校验）
     */
    @GetMapping("/file/access/{applyNo}/{fileId}")
    public Result<Map<String, Object>> fileAccess(@PathVariable String applyNo, @PathVariable Long fileId) {
        // 检查访问权限
        if (!disclosureService.hasFileAccess(applyNo, fileId)) {
            return Result.error("您暂无权限查看该文件，请等待审核通过");
        }
        
        DisclosureFile file = disclosureFileService.getById(fileId);
        if (file == null || file.getDeleted() == 1) {
            return Result.error("文件不存在或已被删除");
        }
        
        DisclosureFileAccess access = disclosureService.getFileAccess(applyNo, fileId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("fileId", file.getId());
        result.put("fileName", file.getFileName());
        result.put("fileCode", file.getFileCode());
        result.put("fileUrl", file.getFileUrl());
        result.put("fileSize", file.getFileSize());
        result.put("fileType", file.getFileType());
        result.put("deptName", file.getDeptName());
        result.put("accessType", access.getAccessType());
        result.put("expireTime", access.getExpireTime() != null ? access.getExpireTime().toString() : "");
        return Result.success(result);
    }
    
    /**
     * C端-根据身份证号查询我的申请列表
     */
    @GetMapping("/my-applications")
    public Result<List<Map<String, Object>>> myApplications(@RequestParam String idCard) {
        List<DisclosureApply> applies = disclosureService.listByIdCard(idCard);
        List<Map<String, Object>> result = applies.stream().map(this::toMap).collect(Collectors.toList());
        return Result.success(result);
    }

    private Map<String, Object> toMap(DisclosureApply a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId() != null ? a.getId() : 0);
        map.put("applyNo", a.getApplyNo() != null ? a.getApplyNo() : "");
        map.put("applicant", a.getApplicant() != null ? a.getApplicant() : "");
        map.put("idCard", a.getIdCard() != null ? a.getIdCard() : "");
        map.put("phone", a.getPhone() != null ? a.getPhone() : "");
        map.put("content", a.getContent() != null ? a.getContent() : "");
        map.put("purpose", a.getPurpose() != null ? a.getPurpose() : "");
        map.put("acquireMethod", a.getAcquireMethod() != null ? a.getAcquireMethod() : "");
        map.put("status", a.getStatus() != null ? a.getStatus() : "");
        map.put("deadline", a.getDeadline() != null ? a.getDeadline().toString() : "");
        map.put("replyContent", a.getReplyContent() != null ? a.getReplyContent() : "");
        map.put("replyBy", a.getReplyBy() != null ? a.getReplyBy() : "");
        map.put("replyTime", a.getReplyTime() != null ? a.getReplyTime().toString() : "");
        map.put("createTime", a.getCreateTime() != null ? a.getCreateTime().toString() : "");
        map.put("fileId", a.getFileId());
        // 关联文件信息
        if (a.getFileId() != null) {
            DisclosureFile file = disclosureFileService.getById(a.getFileId());
            if (file != null && file.getDeleted() == 0) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("id", file.getId());
                fileInfo.put("fileName", file.getFileName());
                fileInfo.put("fileCode", file.getFileCode());
                fileInfo.put("deptName", file.getDeptName());
                map.put("fileInfo", fileInfo);
            }
        }
        return map;
    }
}
