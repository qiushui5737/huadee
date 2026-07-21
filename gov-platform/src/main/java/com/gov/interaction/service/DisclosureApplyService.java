package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.DisclosureApply;
import com.gov.interaction.entity.DisclosureAuditRecord;
import com.gov.interaction.entity.DisclosureFileAccess;
import com.gov.interaction.mapper.DisclosureApplyMapper;
import com.gov.interaction.mapper.DisclosureAuditRecordMapper;
import com.gov.interaction.mapper.DisclosureFileAccessMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * C3-依申请公开服务 & C4-审核答复
 */
@Service
public class DisclosureApplyService extends ServiceImpl<DisclosureApplyMapper, DisclosureApply> {

    private final DisclosureAuditRecordMapper auditRecordMapper;
    private final DisclosureFileAccessMapper fileAccessMapper;

    public DisclosureApplyService(DisclosureAuditRecordMapper auditRecordMapper,
                                   DisclosureFileAccessMapper fileAccessMapper) {
        this.auditRecordMapper = auditRecordMapper;
        this.fileAccessMapper = fileAccessMapper;
    }

    /**
     * C3-提交公开申请
     */
    public DisclosureApply apply(DisclosureApply apply) {
        if (!StringUtils.hasText(apply.getApplicant())) {
            throw BusinessException.of("申请人姓名不能为空");
        }
        if (!StringUtils.hasText(apply.getContent())) {
            throw BusinessException.of("申请内容不能为空");
        }
        // 生成申请编号
        apply.setApplyNo("YA" + System.currentTimeMillis() % 1000000);
        apply.setStatus("已受理");
        // 答复期限：15个工作日（简化为自然日20天）
        apply.setDeadline(LocalDate.now().plusDays(20));
        save(apply);
        
        // 创建受理审计记录
        createAuditRecord(apply.getApplyNo(), "受理申请", "accept", "系统自动受理", "系统");
        
        // 受理时自动授予文件访问权限
        if (apply.getFileId() != null) {
            grantFileAccess(apply.getApplyNo(), apply.getFileId(), apply.getApplicant(), "系统");
        }
        
        return apply;
    }

    /**
     * C3-查询申请进度
     */
    public DisclosureApply getByApplyNo(String applyNo) {
        LambdaQueryWrapper<DisclosureApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisclosureApply::getApplyNo, applyNo);
        DisclosureApply apply = getOne(wrapper);
        if (apply == null) {
            throw BusinessException.of("申请编号不存在");
        }
        return apply;
    }

    /**
     * C4-申请列表（分页）
     */
    public IPage<DisclosureApply> listPage(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<DisclosureApply> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DisclosureApply::getApplicant, keyword)
                    .or().like(DisclosureApply::getApplyNo, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(DisclosureApply::getStatus, status);
        }
        wrapper.orderByDesc(DisclosureApply::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * C4-审核通过/驳回
     */
    public void audit(String applyNo, String action, String replyContent, String operator) {
        DisclosureApply apply = getByApplyNo(applyNo);
        if (!StringUtils.hasText(action)) {
            throw BusinessException.of("审核操作不能为空");
        }
        
        String nodeName = "";
        String auditAction = "";
        
        switch (action) {
            case "approve":
                apply.setStatus("已答复");
                apply.setReplyContent(replyContent);
                apply.setReplyBy(operator);
                apply.setReplyTime(LocalDateTime.now());
                nodeName = "审批通过";
                auditAction = "approve";
                break;
            case "reject":
                apply.setStatus("已驳回");
                apply.setReplyContent(replyContent);
                apply.setReplyBy(operator);
                apply.setReplyTime(LocalDateTime.now());
                nodeName = "审批驳回";
                auditAction = "reject";
                break;
            case "processing":
                apply.setStatus("审核中");
                nodeName = "审核中";
                auditAction = "processing";
                break;
            default:
                throw BusinessException.of("无效的审核操作: " + action);
        }
        updateById(apply);
        
        // 创建审计记录
        createAuditRecord(applyNo, nodeName, auditAction, replyContent, operator);
        
        // 审核通过时，自动授予文件访问权限
        if ("approve".equals(action) && apply.getFileId() != null) {
            grantFileAccess(applyNo, apply.getFileId(), apply.getApplicant(), operator);
        }
    }
    
    /**
     * 创建审计记录
     */
    private void createAuditRecord(String applyNo, String nodeName, String action, String opinion, String operatorName) {
        DisclosureAuditRecord record = new DisclosureAuditRecord();
        record.setApplyNo(applyNo);
        record.setNodeName(nodeName);
        record.setAction(action);
        record.setOpinion(opinion);
        record.setOperatorName(operatorName);
        record.setOperateTime(LocalDateTime.now());
        auditRecordMapper.insert(record);
    }
    
    /**
     * 授予文件访问权限
     */
    private void grantFileAccess(String applyNo, Long fileId, String userName, String grantedByName) {
        DisclosureFileAccess access = new DisclosureFileAccess();
        access.setFileId(fileId);
        access.setApplyNo(applyNo);
        access.setUserName(userName);
        access.setAccessType("view");
        access.setGrantedName(grantedByName);
        access.setGrantTime(LocalDateTime.now());
        access.setExpireTime(LocalDateTime.now().plusDays(30)); // 有效期30天
        access.setStatus("active");
        fileAccessMapper.insert(access);
    }
    
    /**
     * 获取审计记录列表
     */
    public List<DisclosureAuditRecord> getAuditRecords(String applyNo) {
        LambdaQueryWrapper<DisclosureAuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisclosureAuditRecord::getApplyNo, applyNo);
        wrapper.orderByAsc(DisclosureAuditRecord::getOperateTime);
        return auditRecordMapper.selectList(wrapper);
    }
    
    /**
     * 检查文件访问权限
     */
    public boolean hasFileAccess(String applyNo, Long fileId) {
        LambdaQueryWrapper<DisclosureFileAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisclosureFileAccess::getApplyNo, applyNo)
               .eq(DisclosureFileAccess::getFileId, fileId)
               .eq(DisclosureFileAccess::getStatus, "active")
               .gt(DisclosureFileAccess::getExpireTime, LocalDateTime.now());
        return fileAccessMapper.selectCount(wrapper) > 0;
    }
    
    /**
     * 获取文件访问权限信息
     */
    public DisclosureFileAccess getFileAccess(String applyNo, Long fileId) {
        LambdaQueryWrapper<DisclosureFileAccess> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisclosureFileAccess::getApplyNo, applyNo)
               .eq(DisclosureFileAccess::getFileId, fileId)
               .eq(DisclosureFileAccess::getStatus, "active")
               .gt(DisclosureFileAccess::getExpireTime, LocalDateTime.now());
        return fileAccessMapper.selectOne(wrapper);
    }

    /**
     * 各状态统计
     */
    public java.util.Map<String, Long> statusStats() {
        java.util.Map<String, Long> stats = new java.util.LinkedHashMap<>();
        String[] statuses = {"已受理", "审核中", "已答复", "已驳回"};
        for (String s : statuses) {
            LambdaQueryWrapper<DisclosureApply> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DisclosureApply::getStatus, s);
            stats.put(s, count(wrapper));
        }
        return stats;
    }

    /**
     * 超时申请统计
     */
    public long countOverdue() {
        LambdaQueryWrapper<DisclosureApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(DisclosureApply::getDeadline, LocalDate.now())
                .notIn(DisclosureApply::getStatus, "已答复", "已驳回");
        return count(wrapper);
    }
    
    /**
     * 根据身份证号查询申请列表
     */
    public List<DisclosureApply> listByIdCard(String idCard) {
        LambdaQueryWrapper<DisclosureApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DisclosureApply::getIdCard, idCard);
        wrapper.orderByDesc(DisclosureApply::getCreateTime);
        return list(wrapper);
    }
    
    /**
     * 直接修改申请状态（管理端）
     */
    public void updateStatus(String applyNo, String newStatus, String operator) {
        DisclosureApply apply = getByApplyNo(applyNo);
        String oldStatus = apply.getStatus();
        if (oldStatus.equals(newStatus)) {
            return; // 状态未变化，无需处理
        }
        // 校验合法状态
        java.util.Set<String> validStatuses = java.util.Set.of("已受理", "审核中", "已答复", "已驳回");
        if (!validStatuses.contains(newStatus)) {
            throw BusinessException.of("无效的目标状态: " + newStatus);
        }
        apply.setStatus(newStatus);
        updateById(apply);
        // 记录审计日志
        createAuditRecord(applyNo, "状态变更", "status_change",
                "状态由「" + oldStatus + "」变更为「" + newStatus + "」", operator);
        
        // 状态变为已答复或已受理时，自动授予文件访问权限
        if (("已答复".equals(newStatus) || "已受理".equals(newStatus)) && apply.getFileId() != null) {
            // 检查是否已有访问权限
            if (!hasFileAccess(applyNo, apply.getFileId())) {
                grantFileAccess(applyNo, apply.getFileId(), apply.getApplicant(), operator);
            }
        }
    }
}
