
package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.ai.service.SensitiveWordService;
import com.gov.interaction.entity.Message;
import com.gov.interaction.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * C1-留言服务 & C2-分派督办
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {
    private final SensitiveWordService sensitiveWordService;

    public MessageService(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /**
     * 提交留言
     */
    public Message submit(Message message) {
        if (!StringUtils.hasText(message.getTitle())) {
            throw BusinessException.of("留言标题不能为空");
        }
        if (!StringUtils.hasText(message.getContent())) {
            throw BusinessException.of("留言内容不能为空");
        }
        java.util.Map<String, Object> sensitive = sensitiveWordService.check(message.getTitle() + "\n" + message.getContent());
        if (!Boolean.TRUE.equals(sensitive.get("allowed"))) {
            throw BusinessException.of("留言包含禁止发布的敏感内容，请修改后重试");
        }
        message.setTitle(sensitiveWordService.filter(message.getTitle()));
        message.setContent(sensitiveWordService.filter(message.getContent()));
        if (!StringUtils.hasText(message.getType())) {
            message.setType("咨询");
        }
        message.setStatus("待分派");
        if ("review".equals(sensitive.get("action"))) message.setStatus("待复核");
        // 默认处理期限：7天
        message.setDeadline(LocalDateTime.now().plusDays(7));
        save(message);
        return message;
    }

    /**
     * 分页查询留言列表
     */
    public IPage<Message> listPage(String keyword, String status, String type, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Message::getTitle, keyword)
                    .or().like(Message::getContent, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Message::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Message::getType, type);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 热门留言（按创建时间排序取最近10条已回复的）
     */
    public List<Message> hotMessages() {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Message::getStatus, "已回复", "已办结")
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT 10");
        return list(wrapper);
    }

    /**
     * 查询留言详情
     */
    public Message detail(Long id) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        return message;
    }

    /**
     * B端回复留言
     */
    public void reply(Long id, String content, String operator) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        if (!StringUtils.hasText(content)) {
            throw BusinessException.of("回复内容不能为空");
        }
        sensitiveWordService.assertNotBlocked(content);
        message.setReplyContent(sensitiveWordService.filter(content));
        message.setReplyBy(operator);
        message.setReplyTime(LocalDateTime.now());
        message.setStatus("已回复");
        updateById(message);
    }

    /**
     * B端分派留言到部门
     */
    public void dispatch(Long id, String deptCode) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        message.setDeptCode(deptCode);
        message.setStatus("已分派");
        updateById(message);
    }

    /**
     * B端办结留言
     */
    public void finish(Long id) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        message.setStatus("已办结");
        updateById(message);
    }

    /**
     * B端督办留言
     */
    public void supervise(Long id, String operator) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        message.setSupervise(true);
        message.setSuperviseTime(LocalDateTime.now());
        updateById(message);
    }

    /**
     * C端评价留言
     */
    public void rate(Long id, Integer rating, String content) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw BusinessException.of("评价星级必须为1-5");
        }
        message.setRating(rating);
        if (StringUtils.hasText(content)) {
            sensitiveWordService.assertNotBlocked(content);
            message.setRatingContent(sensitiveWordService.filter(content));
        }
        updateById(message);
    }

    /**
     * 超时留言统计
     */
    public long countOverdue() {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Message::getDeadline, LocalDateTime.now())
                .notIn(Message::getStatus, "已办结");
        return count(wrapper);
    }

    /**
     * 各状态留言统计
     */
    public java.util.Map<String, Long> statusStats() {
        java.util.Map<String, Long> stats = new java.util.LinkedHashMap<>();
        String[] statuses = {"待分派", "已分派", "已回复", "已办结"};
        for (String s : statuses) {
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getStatus, s);
            stats.put(s, count(wrapper));
        }
        return stats;
    }
}

