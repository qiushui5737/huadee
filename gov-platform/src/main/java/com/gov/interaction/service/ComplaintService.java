package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Complaint;
import com.gov.interaction.mapper.ComplaintMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 投诉服务
 */
@Service
public class ComplaintService extends ServiceImpl<ComplaintMapper, Complaint> {

    /**
     * 提交投诉
     */
    public Complaint submit(Complaint c) {
        if (!StringUtils.hasText(c.getTitle())) {
            throw BusinessException.of("投诉标题不能为空");
        }
        if (!StringUtils.hasText(c.getRealName())) {
            throw BusinessException.of("投诉人姓名不能为空");
        }
        if (!StringUtils.hasText(c.getPhone())) {
            throw BusinessException.of("联系电话不能为空");
        }
        if (!StringUtils.hasText(c.getContent())) {
            throw BusinessException.of("投诉内容不能为空");
        }
        if (!StringUtils.hasText(c.getComplaintUnitL1())) {
            throw BusinessException.of("请选择投诉单位");
        }
        // 生成投诉单号: TS + 年月日时分秒 + 4位随机数
        String no = "TS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        c.setComplaintNo(no);
        c.setStatus("待受理");
        c.setDeadline(LocalDate.now().plusDays(20));
        save(c);
        return c;
    }

    /**
     * C端-根据投诉单号查询进度（仅本人）
     */
    public Complaint getByComplaintNoAndUserId(String complaintNo, Long userId) {
        if (!StringUtils.hasText(complaintNo)) {
            throw BusinessException.of("投诉单号不能为空");
        }
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getComplaintNo, complaintNo)
                .eq(Complaint::getUserId, userId);
        Complaint c = getOne(wrapper);
        if (c == null) {
            throw BusinessException.of(403, "投诉单号不存在或无权查看该投诉");
        }
        return c;
    }

    /**
     * 根据投诉单号查询（无权限校验，B端管理用）
     */
    public Complaint getByComplaintNo(String complaintNo) {
        if (!StringUtils.hasText(complaintNo)) {
            throw BusinessException.of("投诉单号不能为空");
        }
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getComplaintNo, complaintNo);
        Complaint c = getOne(wrapper);
        if (c == null) {
            throw BusinessException.of("投诉单号不存在");
        }
        return c;
    }

    /**
     * B端-投诉列表（分页）
     */
    public IPage<Complaint> listPage(String keyword, String status, String city, int page, int size) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Complaint::getRealName, keyword)
                    .or().like(Complaint::getComplaintNo, keyword)
                    .or().like(Complaint::getTitle, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Complaint::getStatus, status);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(Complaint::getComplaintUnitL1, city);
        }
        wrapper.orderByDesc(Complaint::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * B端-答复投诉
     */
    public void reply(String complaintNo, String replyContent, String operator) {
        Complaint c = getByComplaintNo(complaintNo);
        if (!StringUtils.hasText(replyContent)) {
            throw BusinessException.of("答复内容不能为空");
        }
        c.setStatus("已答复");
        c.setReplyContent(replyContent);
        c.setReplyBy(operator);
        c.setReplyTime(LocalDateTime.now());
        updateById(c);
    }

    /**
     * B端-修改投诉状态
     */
    public void updateStatus(String complaintNo, String status) {
        Complaint c = getByComplaintNo(complaintNo);
        String[] validStatuses = {"待受理", "处理中", "已答复", "已办结"};
        boolean valid = false;
        for (String st : validStatuses) {
            if (st.equals(status)) { valid = true; break; }
        }
        if (!valid) {
            throw BusinessException.of("无效的状态值: " + status);
        }
        c.setStatus(status);
        updateById(c);
    }

    /**
     * B端-办结投诉
     */
    public void finish(String complaintNo) {
        Complaint c = getByComplaintNo(complaintNo);
        if (!"已答复".equals(c.getStatus())) {
            throw BusinessException.of("只有已答复状态的投诉才能办结");
        }
        c.setStatus("已办结");
        updateById(c);
    }

    /**
     * B端-各状态统计
     */
    public Map<String, Long> statusStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        String[] statuses = {"待受理", "处理中", "已答复", "已办结"};
        for (String st : statuses) {
            LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Complaint::getStatus, st);
            stats.put(st, count(wrapper));
        }
        return stats;
    }

    /**
     * B端-超时统计
     */
    public long countOverdue() {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Complaint::getDeadline, LocalDate.now())
                .notIn(Complaint::getStatus, "已答复", "已办结");
        return count(wrapper);
    }
}
