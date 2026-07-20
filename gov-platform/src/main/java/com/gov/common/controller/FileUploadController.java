package com.gov.common.controller;

import com.gov.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件上传接口
 */
@RestController
@RequestMapping("/api/v1/file")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.error("文件大小不能超过50MB");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension)) {
            return Result.error("不支持的文件类型，仅支持: pdf, doc, docx, xls, xlsx, jpg, jpeg, png, gif, txt");
        }

        try {
            // 创建上传目录（使用绝对路径）
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path filePath = uploadPath.resolve(newFilename);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", originalFilename);
            result.put("fileUrl", "/uploads/" + newFilename);
            result.put("fileSize", file.getSize());
            result.put("fileType", extension);

            return Result.success(result, "文件上传成功");
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam String fileUrl) {
        try {
            // 从 URL 中提取文件名
            String filename = fileUrl.replace("/uploads/", "");
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
    
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
    
            return Result.success();
        } catch (IOException e) {
            return Result.error("文件删除失败：" + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    private boolean isAllowedExtension(String extension) {
        String[] allowedExtensions = {"pdf", "doc", "docx", "xls", "xlsx", "jpg", "jpeg", "png", "gif", "txt"};
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
