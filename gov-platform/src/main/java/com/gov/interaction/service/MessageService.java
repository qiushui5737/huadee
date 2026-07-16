package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Message;
import com.gov.interaction.mapper.MessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * C1-留言服务
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

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
        message.setStatus("待分派");
        save(message);
        return message;
    }

    /**
     * 分页查询留言列表
     */
    public IPage<Message> listPage(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Message::getTitle, keyword)
                    .or().like(Message::getContent, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Message::getStatus, status);
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
        message.setReplyContent(content);
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
}
