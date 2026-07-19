package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Suggestion;
import com.gov.interaction.mapper.SuggestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 建议服务
 */
@Service
public class SuggestionService extends ServiceImpl<SuggestionMapper, Suggestion> {

    /**
     * 提交建议
     */
    public Suggestion submit(Suggestion s) {
        if (!StringUtils.hasText(s.getRealName())) {
            throw BusinessException.of("建议人姓名不能为空");
        }
        if (!StringUtils.hasText(s.getPhone())) {
            throw BusinessException.of("联系电话不能为空");
        }
        if (!StringUtils.hasText(s.getTitle())) {
            throw BusinessException.of("建议标题不能为空");
        }
        if (!StringUtils.hasText(s.getContent())) {
            throw BusinessException.of("建议内容不能为空");
        }
        if (!StringUtils.hasText(s.getCity())) {
            throw BusinessException.of("事件发生地不能为空");
        }
        // 生成建议单号: JY + 年月日时分秒 + 4位随机数
        String no = "JY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
        s.setSuggestNo(no);
        s.setStatus("待受理");
        // 答复期限: 15个工作日（简化为自然日20天）
        s.setDeadline(LocalDate.now().plusDays(20));
        save(s);
        return s;
    }

    /**
     * C端-根据建议单号查询进度（仅本人）
     */
    public Suggestion getBySuggestNoAndUserId(String suggestNo, Long userId) {
        if (!StringUtils.hasText(suggestNo)) {
            throw BusinessException.of("建议单号不能为空");
        }
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Suggestion::getSuggestNo, suggestNo)
                .eq(Suggestion::getUserId, userId);
        Suggestion s = getOne(wrapper);
        if (s == null) {
            throw BusinessException.of(403, "建议单号不存在或无权查看该建议");
        }
        return s;
    }

    /**
     * 根据建议单号查询（无权限校验，B端管理用）
     */
    public Suggestion getBySuggestNo(String suggestNo) {
        if (!StringUtils.hasText(suggestNo)) {
            throw BusinessException.of("建议单号不能为空");
        }
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Suggestion::getSuggestNo, suggestNo);
        Suggestion s = getOne(wrapper);
        if (s == null) {
            throw BusinessException.of("建议单号不存在");
        }
        return s;
    }

    /**
     * B端-建议列表（分页）
     */
    public IPage<Suggestion> listPage(String keyword, String status, String city, int page, int size) {
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Suggestion::getRealName, keyword)
                    .or().like(Suggestion::getSuggestNo, keyword)
                    .or().like(Suggestion::getTitle, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Suggestion::getStatus, status);
        }
        if (StringUtils.hasText(city)) {
            wrapper.eq(Suggestion::getCity, city);
        }
        wrapper.orderByDesc(Suggestion::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * B端-答复建议
     */
    public void reply(String suggestNo, String replyContent, String operator) {
        Suggestion s = getBySuggestNo(suggestNo);
        if (!StringUtils.hasText(replyContent)) {
            throw BusinessException.of("答复内容不能为空");
        }
        s.setStatus("已答复");
        s.setReplyContent(replyContent);
        s.setReplyBy(operator);
        s.setReplyTime(LocalDateTime.now());
        updateById(s);
    }

    /**
     * B端-修改建议状态
     */
    public void updateStatus(String suggestNo, String status) {
        Suggestion s = getBySuggestNo(suggestNo);
        String[] validStatuses = {"待受理", "处理中", "已答复", "已办结"};
        boolean valid = false;
        for (String st : validStatuses) {
            if (st.equals(status)) { valid = true; break; }
        }
        if (!valid) {
            throw BusinessException.of("无效的状态值: " + status);
        }
        s.setStatus(status);
        updateById(s);
    }

    /**
     * B端-办结建议
     */
    public void finish(String suggestNo) {
        Suggestion s = getBySuggestNo(suggestNo);
        if (!"已答复".equals(s.getStatus())) {
            throw BusinessException.of("只有已答复状态的建议才能办结");
        }
        s.setStatus("已办结");
        updateById(s);
    }

    /**
     * B端-各状态统计
     */
    public Map<String, Long> statusStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        String[] statuses = {"待受理", "处理中", "已答复", "已办结"};
        for (String st : statuses) {
            LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Suggestion::getStatus, st);
            stats.put(st, count(wrapper));
        }
        return stats;
    }

    /**
     * B端-超时统计
     */
    public long countOverdue() {
        LambdaQueryWrapper<Suggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Suggestion::getDeadline, LocalDate.now())
                .notIn(Suggestion::getStatus, "已答复", "已办结");
        return count(wrapper);
    }
}
