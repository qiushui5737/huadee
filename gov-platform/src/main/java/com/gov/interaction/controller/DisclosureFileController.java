package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.DisclosureFile;
import com.gov.interaction.service.DisclosureFileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 保密文件管理接口（B端）
 */
@RestController
@RequestMapping("/api/v1/disclosure/file")
public class DisclosureFileController {

    private final DisclosureFileService disclosureFileService;

    public DisclosureFileController(DisclosureFileService disclosureFileService) {
        this.disclosureFileService = disclosureFileService;
    }

    /**
     * B端-创建文件
     */
    @PostMapping
    public Result<DisclosureFile> create(@RequestBody DisclosureFile file) {
        DisclosureFile saved = disclosureFileService.createFile(file);
        return Result.success(saved, "文件创建成功");
    }

    /**
     * B端-更新文件
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DisclosureFile file) {
        file.setId(id);
        disclosureFileService.updateFile(file);
        return Result.success();
    }

    /**
     * B端-删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        disclosureFileService.deleteFile(id);
        return Result.success();
    }

    /**
     * B端-文件列表（分页）
     */
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<DisclosureFile> pageResult = disclosureFileService.listPage(keyword, category, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * B端-文件详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        DisclosureFile file = disclosureFileService.getFileDetail(id);
        return Result.success(toMap(file));
    }

    /**
     * B端-公开文件
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, @RequestParam(defaultValue = "管理员") String operator) {
        disclosureFileService.publishFile(id, operator);
        return Result.success();
    }

    /**
     * B端-统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(disclosureFileService.stats());
    }

    /**
     * C端-公开文件列表（供申请请选择）
     */
    @GetMapping("/public/list")
    public Result<PageResult<Map<String, Object>>> publicList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<DisclosureFile> pageResult = disclosureFileService.listPublicFiles(keyword, category, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toPublicMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * C端-公开文件详情
     */
    @GetMapping("/public/{id}")
    public Result<Map<String, Object>> publicDetail(@PathVariable Long id) {
        DisclosureFile file = disclosureFileService.getFileDetail(id);
        return Result.success(toPublicMap(file));
    }

    private Map<String, Object> toMap(DisclosureFile f) {
        return Map.ofEntries(
                Map.entry("id", f.getId() != null ? f.getId() : 0),
                Map.entry("fileCode", f.getFileCode() != null ? f.getFileCode() : ""),
                Map.entry("fileName", f.getFileName() != null ? f.getFileName() : ""),
                Map.entry("fileCategory", f.getFileCategory() != null ? f.getFileCategory() : ""),
                Map.entry("fileType", f.getFileType() != null ? f.getFileType() : ""),
                Map.entry("fileSize", f.getFileSize() != null ? f.getFileSize() : 0),
                Map.entry("fileUrl", f.getFileUrl() != null ? f.getFileUrl() : ""),
                Map.entry("description", f.getDescription() != null ? f.getDescription() : ""),
                Map.entry("deptCode", f.getDeptCode() != null ? f.getDeptCode() : ""),
                Map.entry("deptName", f.getDeptName() != null ? f.getDeptName() : ""),
                Map.entry("confidentialityLevel", f.getConfidentialityLevel() != null ? f.getConfidentialityLevel() : ""),
                Map.entry("status", f.getStatus() != null ? f.getStatus() : ""),
                Map.entry("publishTime", f.getPublishTime() != null ? f.getPublishTime().toString() : ""),
                Map.entry("createTime", f.getCreateTime() != null ? f.getCreateTime().toString() : "")
        );
    }

    /**
     * C端文件信息（不暴露下载链接）
     */
    private Map<String, Object> toPublicMap(DisclosureFile f) {
        return Map.ofEntries(
                Map.entry("id", f.getId() != null ? f.getId() : 0),
                Map.entry("fileCode", f.getFileCode() != null ? f.getFileCode() : ""),
                Map.entry("fileName", f.getFileName() != null ? f.getFileName() : ""),
                Map.entry("fileCategory", f.getFileCategory() != null ? f.getFileCategory() : ""),
                Map.entry("deptName", f.getDeptName() != null ? f.getDeptName() : ""),
                Map.entry("description", f.getDescription() != null ? f.getDescription() : ""),
                Map.entry("confidentialityLevel", f.getConfidentialityLevel() != null ? f.getConfidentialityLevel() : ""),
                Map.entry("createTime", f.getCreateTime() != null ? f.getCreateTime().toString() : "")
        );
    }
}
