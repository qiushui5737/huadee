package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.DisclosureApply;
import com.gov.interaction.mapper.DisclosureApplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * C3-依申请公开服务 & C4-审核答复
 */
@Service
public class DisclosureApplyService extends ServiceImpl<DisclosureApplyMapper, DisclosureApply> {

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
        switch (action) {
            case "approve":
                apply.setStatus("已答复");
                apply.setReplyContent(replyContent);
                apply.setReplyBy(operator);
                apply.setReplyTime(LocalDateTime.now());
                break;
            case "reject":
                apply.setStatus("已驳回");
                apply.setReplyContent(replyContent);
                apply.setReplyBy(operator);
                apply.setReplyTime(LocalDateTime.now());
                break;
            case "processing":
                apply.setStatus("审核中");
                break;
            default:
                throw BusinessException.of("无效的审核操作: " + action);
        }
        updateById(apply);
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
}
