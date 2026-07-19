package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Consultation;
import com.gov.interaction.mapper.ConsultationMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 我要咨询 - 咨询服务
 */
@Service
public class ConsultationService extends ServiceImpl<ConsultationMapper, Consultation> {

    /**
     * 提交咨询
     */
    public Consultation submit(Consultation c) {
        if (!StringUtils.hasText(c.getRealName())) {
            throw BusinessException.of("真实姓名不能为空");
        }
        if (!StringUtils.hasText(c.getPhone())) {
            throw BusinessException.of("手机号码不能为空");
        }
        if (!StringUtils.hasText(c.getTitle())) {
            throw BusinessException.of("咨询主题不能为空");
        }
        if (!StringUtils.hasText(c.getCity())) {
            throw BusinessException.of("咨询市州不能为空");
        }
        if (!StringUtils.hasText(c.getContent())) {
            throw BusinessException.of("咨询内容不能为空");
        }
        // 生成咨询单号: ZX + 年月日时分秒 + 4位随机数
        String no = "ZX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        c.setConsultNo(no);
        c.setStatus("待受理");
        // 答复期限: 15个工作日（简化为自然日20天）
        c.setDeadline(LocalDate.now().plusDays(20));
        save(c);
        return c;
    }

    /**
     * 根据咨询单号查询（仅本人）
     */
    public Consultation getByConsultNoAndUserId(String consultNo, Long userId) {
        if (!StringUtils.hasText(consultNo)) {
            throw BusinessException.of("咨询单号不能为空");
        }
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getConsultNo, consultNo)
                .eq(Consultation::getUserId, userId);
        Consultation c = getOne(wrapper);
        if (c == null) {
            throw BusinessException.of(403, "咨询单号不存在或无权查看该咨询单");
        }
        return c;
    }

    /**
     * 根据咨询单号查询（无权限校验，B端管理用）
     */
    public Consultation getByConsultNo(String consultNo) {
        if (!StringUtils.hasText(consultNo)) {
            throw BusinessException.of("咨询单号不能为空");
        }
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getConsultNo, consultNo);
        Consultation c = getOne(wrapper);
        if (c == null) {
            throw BusinessException.of("咨询单号不存在");
        }
        return c;
    }

    /**
     * B端-咨询列表（分页）
     */
    public IPage<Consultation> listPage(String keyword, String status, String city, int page, int size) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Consultation::getRealName, keyword)
                    .or().like(Consultation::getConsultNo, keyword)
                    .or().like(Consultation::getTitle, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Consultation::getStatus, status);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(Consultation::getCity, city);
        }
        wrapper.orderByDesc(Consultation::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * B端-答复咨询
     */
    public void reply(String consultNo, String replyContent, String operator) {
        Consultation c = getByConsultNo(consultNo);
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
     * B端-修改咨询状态
     */
    public void updateStatus(String consultNo, String status) {
        Consultation c = getByConsultNo(consultNo);
        String[] validStatuses = {"待受理", "处理中", "已答复", "已办结"};
        boolean valid = false;
        for (String s : validStatuses) {
            if (s.equals(status)) { valid = true; break; }
        }
        if (!valid) {
            throw BusinessException.of("无效的状态值: " + status);
        }
        c.setStatus(status);
        updateById(c);
    }

    /**
     * B端-办结咨询
     */
    public void finish(String consultNo) {
        Consultation c = getByConsultNo(consultNo);
        if (!"已答复".equals(c.getStatus())) {
            throw BusinessException.of("只有已答复状态的咨询才能办结");
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
        for (String s : statuses) {
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Consultation::getStatus, s);
            stats.put(s, count(wrapper));
        }
        return stats;
    }

    /**
     * B端-超时统计
     */
    public long countOverdue() {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Consultation::getDeadline, LocalDate.now())
                .notIn(Consultation::getStatus, "已答复", "已办结");
        return count(wrapper);
    }
}
