package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.DisclosureFile;
import com.gov.interaction.mapper.DisclosureFileMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 保密文件服务
 */
@Service
public class DisclosureFileService extends ServiceImpl<DisclosureFileMapper, DisclosureFile> {

    /**
     * B端-创建文件
     */
    public DisclosureFile createFile(DisclosureFile file) {
        if (!StringUtils.hasText(file.getFileName())) {
            throw BusinessException.of("文件名称不能为空");
        }
        // 生成文件编号
        if (!StringUtils.hasText(file.getFileCode())) {
            file.setFileCode("DF" + System.currentTimeMillis() % 1000000);
        }
        if (!StringUtils.hasText(file.getStatus())) {
            file.setStatus("secret");
        }
        if (!StringUtils.hasText(file.getConfidentialityLevel())) {
            file.setConfidentialityLevel("secret");
        }
        save(file);
        return file;
    }

    /**
     * B端-更新文件
     */
    public void updateFile(DisclosureFile file) {
        if (file.getId() == null) {
            throw BusinessException.of("文件ID不能为空");
        }
        updateById(file);
    }

    /**
     * B端-删除文件（逻辑删除）
     */
    public void deleteFile(Long id) {
        DisclosureFile file = getById(id);
        if (file == null) {
            throw BusinessException.of("文件不存在");
        }
        file.setDeleted(1);
        updateById(file);
    }

    /**
     * B端-文件列表（分页）
     */
    public IPage<DisclosureFile> listPage(String keyword, String category, String status, int page, int size) {
        LambdaQueryWrapper<DisclosureFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DisclosureFile::getFileName, keyword)
                    .or().like(DisclosureFile::getFileCode, keyword)
                    .or().like(DisclosureFile::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(DisclosureFile::getFileCategory, category);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(DisclosureFile::getStatus, status);
        }
        wrapper.eq(DisclosureFile::getDeleted, 0);
        wrapper.orderByDesc(DisclosureFile::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * B端-文件详情
     */
    public DisclosureFile getFileDetail(Long id) {
        DisclosureFile file = getById(id);
        if (file == null || file.getDeleted() == 1) {
            throw BusinessException.of("文件不存在");
        }
        return file;
    }

    /**
     * B端-公开文件
     */
    public void publishFile(Long id, String operator) {
        DisclosureFile file = getById(id);
        if (file == null) {
            throw BusinessException.of("文件不存在");
        }
        file.setStatus("public");
        file.setPublishTime(LocalDateTime.now());
        updateById(file);
    }

    /**
     * B端-统计
     */
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 总数
        LambdaQueryWrapper<DisclosureFile> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(DisclosureFile::getDeleted, 0);
        long total = count(totalWrapper);

        // 各状态数量
        LambdaQueryWrapper<DisclosureFile> secretWrapper = new LambdaQueryWrapper<>();
        secretWrapper.eq(DisclosureFile::getDeleted, 0).eq(DisclosureFile::getStatus, "secret");
        long secretCount = count(secretWrapper);

        LambdaQueryWrapper<DisclosureFile> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(DisclosureFile::getDeleted, 0).eq(DisclosureFile::getStatus, "approved");
        long approvedCount = count(approvedWrapper);

        LambdaQueryWrapper<DisclosureFile> publicWrapper = new LambdaQueryWrapper<>();
        publicWrapper.eq(DisclosureFile::getDeleted, 0).eq(DisclosureFile::getStatus, "public");
        long publicCount = count(publicWrapper);

        result.put("total", total);
        result.put("secretCount", secretCount);
        result.put("approvedCount", approvedCount);
        result.put("publicCount", publicCount);

        return result;
    }

    /**
     * C端-公开文件列表（供申请请选择）
     */
    public IPage<DisclosureFile> listPublicFiles(String keyword, String category, int page, int size) {
        LambdaQueryWrapper<DisclosureFile> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DisclosureFile::getFileName, keyword)
                    .or().like(DisclosureFile::getFileCode, keyword)
                    .or().like(DisclosureFile::getDescription, keyword));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(DisclosureFile::getFileCategory, category);
        }
        wrapper.eq(DisclosureFile::getDeleted, 0);
        wrapper.orderByDesc(DisclosureFile::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}
