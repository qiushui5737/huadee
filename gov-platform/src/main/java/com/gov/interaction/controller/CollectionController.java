package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.common.utils.JwtUtil;
import com.gov.interaction.entity.Collection;
import com.gov.interaction.entity.CollectionOpinion;
import com.gov.interaction.service.CollectionService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意见征集接口（C端查看/提交 + B端管理）
 */
@RestController
@RequestMapping("/api/v1/collection")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    // ========== C端接口 ==========

    /**
     * C端-征集列表
     */
    @GetMapping("/public")
    public Result<PageResult<Map<String, Object>>> publicList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Collection> pageResult = collectionService.listPublic(keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C端-征集详情
     */
    @GetMapping("/public/{id}")
    public Result<Map<String, Object>> publicDetail(@PathVariable Long id) {
        Collection c = collectionService.detail(id);
        Map<String, Object> data = toMap(c);
        // 附加意见统计
        Map<String, Long> stats = collectionService.opinionStats(id);
        data.put("opinionCount", stats.get("total"));
        data.put("pendingCount", stats.get("pending"));
        data.put("repliedCount", stats.get("replied"));
        return Result.success(data);
    }

    /**
     * C端-结果反馈公示页面
     */
    @GetMapping("/public/{id}/feedback")
    public Result<Map<String, Object>> publicFeedback(@PathVariable Long id) {
        Collection c = collectionService.detail(id);
        Map<String, Object> data = toMap(c);
        Map<String, Long> stats = collectionService.opinionStats(id);
        data.put("total", stats.get("total"));
        data.put("adopted", stats.get("adopted"));
        data.put("partiallyAdopted", stats.get("partiallyAdopted"));
        data.put("rejected", stats.get("rejected"));
        return Result.success(data);
    }

    /**
     * C端-提交意见
     */
    @PostMapping("/{id}/opinion")
    public Result<Map<String, Object>> submitOpinion(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        CollectionOpinion opinion = new CollectionOpinion();
        opinion.setCollectionId(id);
        opinion.setTitle((String) body.get("title"));
        opinion.setRealName((String) body.get("realName"));
        opinion.setPhone((String) body.get("phone"));
        opinion.setIdCard((String) body.get("idCard"));
        opinion.setAddress((String) body.get("address"));
        opinion.setContent((String) body.get("content"));
        // 记录当前用户ID
        try {
            String token = (String) request.getAttribute("jwtToken");
            if (token != null) {
                Long userId = JwtUtil.getUserId(token);
                opinion.setUserId(userId);
            }
        } catch (Exception ignored) {}

        CollectionOpinion saved = collectionService.submitOpinion(opinion);
        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("status", saved.getStatus());
        return Result.success(result, "意见提交成功");
    }

    // ========== B端接口 ==========

    /**
     * B端-征集列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Collection> pageResult = collectionService.listPage(keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-创建征集
     */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Collection c = new Collection();
        c.setTitle((String) body.get("title"));
        c.setDescription((String) body.get("description"));
        c.setDeptName((String) body.get("deptName"));
        c.setContactName((String) body.get("contactName"));
        c.setContactPhone((String) body.get("contactPhone"));
        c.setAttachmentName((String) body.get("attachmentName"));
        c.setAttachmentUrl((String) body.get("attachmentUrl"));
        if (body.get("startDate") != null) {
            c.setStartDate(LocalDate.parse(body.get("startDate").toString()));
        }
        if (body.get("endDate") != null) {
            c.setEndDate(LocalDate.parse(body.get("endDate").toString()));
        }
        Collection saved = collectionService.create(c);
        return Result.success(toMap(saved), "创建成功");
    }

    /**
     * B端-更新征集
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Collection c = collectionService.getById(id);
        if (c == null) {
            return Result.error(404, "征集不存在");
        }
        c.setTitle((String) body.get("title"));
        c.setDescription((String) body.get("description"));
        c.setDeptName((String) body.get("deptName"));
        c.setContactName((String) body.get("contactName"));
        c.setContactPhone((String) body.get("contactPhone"));
        c.setAttachmentName((String) body.get("attachmentName"));
        c.setAttachmentUrl((String) body.get("attachmentUrl"));
        if (body.get("startDate") != null) {
            c.setStartDate(LocalDate.parse(body.get("startDate").toString()));
        }
        if (body.get("endDate") != null) {
            c.setEndDate(LocalDate.parse(body.get("endDate").toString()));
        }
        collectionService.update(c);
        return Result.success();
    }

    /**
     * B端-删除征集
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        collectionService.delete(id);
        return Result.success();
    }

    /**
     * B端-发布征集
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        collectionService.publish(id);
        return Result.success();
    }

    /**
     * B端-结束征集
     */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        collectionService.finish(id);
        return Result.success();
    }

    /**
     * B端-添加结果反馈
     */
    @PostMapping("/{id}/feedback")
    public Result<Void> addFeedback(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String feedback = body.get("feedback");
        collectionService.addFeedback(id, feedback);
        return Result.success();
    }

    /**
     * B端-征集下的意见列表
     */
    @GetMapping("/{id}/opinions")
    public Result<PageResult<Map<String, Object>>> listOpinions(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<CollectionOpinion> pageResult = collectionService.listOpinions(id, keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::opinionToMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-处理意见（采纳/不采纳）
     */
    @PostMapping("/opinion/{opinionId}/reply")
    public Result<Void> replyOpinion(@PathVariable Long opinionId, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        String operator = body.getOrDefault("operator", "系统管理员");
        String status = body.getOrDefault("status", "已采纳");
        collectionService.replyOpinion(opinionId, replyContent, operator, status);
        return Result.success();
    }

    /**
     * B端-意见统计
     */
    @GetMapping("/{id}/opinion-stats")
    public Result<Map<String, Long>> opinionStats(@PathVariable Long id) {
        return Result.success(collectionService.opinionStats(id));
    }

    // ========== 转换方法 ==========

    private Map<String, Object> toMap(Collection c) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", c.getId() != null ? c.getId() : 0);
        map.put("title", c.getTitle() != null ? c.getTitle() : "");
        map.put("description", c.getDescription() != null ? c.getDescription() : "");
        map.put("deptName", c.getDeptName() != null ? c.getDeptName() : "");
        map.put("contactName", c.getContactName() != null ? c.getContactName() : "");
        map.put("contactPhone", c.getContactPhone() != null ? c.getContactPhone() : "");
        map.put("attachmentName", c.getAttachmentName() != null ? c.getAttachmentName() : "");
        map.put("attachmentUrl", c.getAttachmentUrl() != null ? c.getAttachmentUrl() : "");
        map.put("startDate", c.getStartDate() != null ? c.getStartDate().toString() : "");
        map.put("endDate", c.getEndDate() != null ? c.getEndDate().toString() : "");
        map.put("status", c.getStatus() != null ? c.getStatus() : "");
        map.put("feedback", c.getFeedback() != null ? c.getFeedback() : "");
        map.put("feedbackTime", c.getFeedbackTime() != null ? c.getFeedbackTime().toString() : "");
        map.put("createTime", c.getCreateTime() != null ? c.getCreateTime().toString() : "");
        return map;
    }

    private Map<String, Object> opinionToMap(CollectionOpinion o) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", o.getId() != null ? o.getId() : 0);
        map.put("collectionId", o.getCollectionId() != null ? o.getCollectionId() : 0);
        map.put("title", o.getTitle() != null ? o.getTitle() : "");
        map.put("realName", o.getRealName() != null ? o.getRealName() : "");
        map.put("phone", o.getPhone() != null ? o.getPhone() : "");
        map.put("idCard", o.getIdCard() != null ? o.getIdCard() : "");
        map.put("address", o.getAddress() != null ? o.getAddress() : "");
        map.put("content", o.getContent() != null ? o.getContent() : "");
        map.put("status", o.getStatus() != null ? o.getStatus() : "");
        map.put("replyContent", o.getReplyContent() != null ? o.getReplyContent() : "");
        map.put("replyBy", o.getReplyBy() != null ? o.getReplyBy() : "");
        map.put("replyTime", o.getReplyTime() != null ? o.getReplyTime().toString() : "");
        map.put("createTime", o.getCreateTime() != null ? o.getCreateTime().toString() : "");
        return map;
    }
}
