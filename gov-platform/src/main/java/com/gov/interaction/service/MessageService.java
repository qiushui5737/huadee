package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Message;
import com.gov.interaction.entity.MessageReply;
import com.gov.interaction.mapper.MessageMapper;
import com.gov.interaction.mapper.MessageReplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * C1-留言服务 & C2-分派督办
 */
@Service
public class MessageService extends ServiceImpl<MessageMapper, Message> {

    private final MessageReplyMapper messageReplyMapper;

    public MessageService(MessageReplyMapper messageReplyMapper) {
        this.messageReplyMapper = messageReplyMapper;
    }

    /**
     * 提交留言（生成信件单号）
     */
    public Message submit(Message message) {
        if (!StringUtils.hasText(message.getTitle())) {
            throw BusinessException.of("留言标题不能为空");
        }
        if (!StringUtils.hasText(message.getContent())) {
            throw BusinessException.of("留言内容不能为空");
        }
        if (!StringUtils.hasText(message.getType())) {
            message.setType("咨询");
        }
        message.setStatus("待分派");
        // 默认公开
        if (message.getIsPublic() == null) {
            message.setIsPublic(true);
        }
        // 生成信件单号: LX + 年月日时分秒 + 4位随机数
        String no = "LX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        message.setConsultNo(no);
        // 默认处理期限：7天
        message.setDeadline(LocalDateTime.now().plusDays(7));
        save(message);
        return message;
    }

    /**
     * 分页查询留言列表（C端：只显示公开的 + 自己的）
     */
    public IPage<Message> listPage(String keyword, String consultNo, String status, String type, Long currentUserId, Boolean isPublicOnly, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        // 可见性过滤
        if (Boolean.TRUE.equals(isPublicOnly)) {
            // 只显示公开信件
            wrapper.eq(Message::getIsPublic, true);
        } else if (currentUserId != null) {
            // 公开的 OR 自己的
            wrapper.and(w -> w.eq(Message::getIsPublic, true)
                    .or().eq(Message::getUserId, currentUserId));
        } else {
            wrapper.eq(Message::getIsPublic, true);
        }
        // 信件单号精确查询
        if (StringUtils.hasText(consultNo)) {
            wrapper.eq(Message::getConsultNo, consultNo.trim());
        }
        // 关键词模糊查询
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
     * 管理端分页查询（全部信件）
     */
    public IPage<Message> listPageAdmin(String keyword, String status, String type, int page, int size) {
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
     * 查询留言详情（C端需校验可见性）
     */
    public Message detail(Long id, Long currentUserId) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        // 私密信件只有本人和管理员可查看
        if (Boolean.FALSE.equals(message.getIsPublic())) {
            if (currentUserId == null || !currentUserId.equals(message.getUserId())) {
                throw BusinessException.of(403, "该信件为私密信件，无权查看");
            }
        }
        return message;
    }

    /**
     * 查询留言详情（管理端，不校验可见性）
     */
    public Message detail(Long id) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        return message;
    }

    /**
     * B端回复留言（追加到对话记录）
     */
    public void reply(Long id, String content, String operator) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        if (!StringUtils.hasText(content)) {
            throw BusinessException.of("回复内容不能为空");
        }
        // 插入回复记录到对话表
        MessageReply reply = new MessageReply();
        reply.setMessageId(id);
        reply.setUserName(operator);
        reply.setUserType("ADMIN");
        reply.setContent(content);
        reply.setCreateTime(LocalDateTime.now());
        messageReplyMapper.insert(reply);

        // 同时更新主表的最新回复信息（保持向后兼容）
        message.setReplyContent(content);
        message.setReplyBy(operator);
        message.setReplyTime(LocalDateTime.now());
        message.setStatus("已回复");
        updateById(message);
    }

    /**
     * C端用户回复（追加到对话记录，仅信件本人可回复）
     */
    public void userReply(Long id, Long userId, String userName, String content) {
        Message message = getById(id);
        if (message == null) {
            throw BusinessException.of("留言不存在");
        }
        // 校验是否为信件本人
        if (userId == null || !userId.equals(message.getUserId())) {
            throw BusinessException.of(403, "只有信件本人才能回复");
        }
        if (!StringUtils.hasText(content)) {
            throw BusinessException.of("回复内容不能为空");
        }
        // 插入用户回复记录
        MessageReply reply = new MessageReply();
        reply.setMessageId(id);
        reply.setUserId(userId);
        reply.setUserName(userName);
        reply.setUserType("USER");
        reply.setContent(content);
        reply.setCreateTime(LocalDateTime.now());
        messageReplyMapper.insert(reply);
    }

    /**
     * 获取留言的所有回复记录（按时间升序）
     */
    public List<MessageReply> listReplies(Long messageId) {
        LambdaQueryWrapper<MessageReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageReply::getMessageId, messageId)
                .orderByAsc(MessageReply::getCreateTime);
        return messageReplyMapper.selectList(wrapper);
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
        message.setRatingContent(content);
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
