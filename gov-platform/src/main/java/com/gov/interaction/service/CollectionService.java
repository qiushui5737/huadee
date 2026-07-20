package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.Collection;
import com.gov.interaction.entity.CollectionOpinion;
import com.gov.interaction.mapper.CollectionMapper;
import com.gov.interaction.mapper.CollectionOpinionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意见征集服务
 */
@Service
public class CollectionService extends ServiceImpl<CollectionMapper, Collection> {

    private final CollectionOpinionMapper opinionMapper;

    public CollectionService(CollectionOpinionMapper opinionMapper) {
        this.opinionMapper = opinionMapper;
    }

    // ========== 征集管理 ==========

    /**
     * B端-分页查询征集列表
     */
    public IPage<Collection> listPage(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Collection::getTitle, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Collection::getStatus, status);
        }
        wrapper.orderByDesc(Collection::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * B端-创建征集
     */
    public Collection create(Collection collection) {
        if (!StringUtils.hasText(collection.getTitle())) {
            throw BusinessException.of("征集主题不能为空");
        }
        if (collection.getStartDate() == null || collection.getEndDate() == null) {
            throw BusinessException.of("征集时间不能为空");
        }
        if (collection.getStartDate().isAfter(collection.getEndDate())) {
            throw BusinessException.of("开始日期不能晚于结束日期");
        }
        // 根据日期自动设置状态
        LocalDate today = LocalDate.now();
        if (today.isBefore(collection.getStartDate())) {
            collection.setStatus("未开始");
        } else if (today.isAfter(collection.getEndDate())) {
            collection.setStatus("已结束");
        } else {
            collection.setStatus("进行中");
        }
        save(collection);
        return collection;
    }

    /**
     * B端-更新征集
     */
    public void update(Collection collection) {
        if (collection.getId() == null) {
            throw BusinessException.of("征集ID不能为空");
        }
        if (collection.getStartDate() != null && collection.getEndDate() != null
                && collection.getStartDate().isAfter(collection.getEndDate())) {
            throw BusinessException.of("开始日期不能晚于结束日期");
        }
        updateById(collection);
    }

    /**
     * B端-删除征集
     */
    public void delete(Long id) {
        removeById(id);
    }

    /**
     * B端-发布征集（设置状态为进行中）
     */
    public void publish(Long id) {
        Collection c = getById(id);
        if (c == null) {
            throw BusinessException.of("征集不存在");
        }
        c.setStatus("进行中");
        updateById(c);
    }

    /**
     * B端-结束征集
     */
    public void finish(Long id) {
        Collection c = getById(id);
        if (c == null) {
            throw BusinessException.of("征集不存在");
        }
        c.setStatus("已结束");
        updateById(c);
    }

    /**
     * B端-添加结果反馈
     */
    public void addFeedback(Long id, String feedback) {
        Collection c = getById(id);
        if (c == null) {
            throw BusinessException.of("征集不存在");
        }
        c.setFeedback(feedback);
        c.setFeedbackTime(LocalDateTime.now());
        updateById(c);
    }

    // ========== 意见管理 ==========

    /**
     * B端-分页查询某征集下的意见
     */
    public IPage<CollectionOpinion> listOpinions(Long collectionId, String keyword, String status, int page, int size) {
        LambdaQueryWrapper<CollectionOpinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionOpinion::getCollectionId, collectionId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CollectionOpinion::getTitle, keyword)
                    .or().like(CollectionOpinion::getRealName, keyword)
                    .or().like(CollectionOpinion::getContent, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(CollectionOpinion::getStatus, status);
        }
        wrapper.orderByDesc(CollectionOpinion::getCreateTime);
        return opinionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * B端-处理意见（采纳/不采纳）
     */
    public void replyOpinion(Long opinionId, String replyContent, String operator, String status) {
        CollectionOpinion opinion = opinionMapper.selectById(opinionId);
        if (opinion == null) {
            throw BusinessException.of("意见不存在");
        }
        if (!StringUtils.hasText(replyContent)) {
            throw BusinessException.of("反馈理由不能为空");
        }
        if (!"已采纳".equals(status) && !"部分采纳".equals(status) && !"不采纳".equals(status)) {
            throw BusinessException.of("处理状态只能是已采纳、部分采纳或不采纳");
        }
        opinion.setReplyContent(replyContent);
        opinion.setReplyBy(operator);
        opinion.setReplyTime(LocalDateTime.now());
        opinion.setStatus(status);
        opinionMapper.updateById(opinion);
    }

    /**
     * B端-统计某征集的意见数量
     */
    public Map<String, Long> opinionStats(Long collectionId) {
        Map<String, Long> stats = new HashMap<>();
        LambdaQueryWrapper<CollectionOpinion> baseWrapper = new LambdaQueryWrapper<>();
        baseWrapper.eq(CollectionOpinion::getCollectionId, collectionId);
        stats.put("total", opinionMapper.selectCount(baseWrapper));

        LambdaQueryWrapper<CollectionOpinion> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .eq(CollectionOpinion::getStatus, "待处理");
        stats.put("pending", opinionMapper.selectCount(pendingWrapper));

        LambdaQueryWrapper<CollectionOpinion> repliedWrapper = new LambdaQueryWrapper<>();
        repliedWrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .in(CollectionOpinion::getStatus, "已采纳", "部分采纳", "不采纳");
        stats.put("replied", opinionMapper.selectCount(repliedWrapper));

        LambdaQueryWrapper<CollectionOpinion> adoptedWrapper = new LambdaQueryWrapper<>();
        adoptedWrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .eq(CollectionOpinion::getStatus, "已采纳");
        stats.put("adopted", opinionMapper.selectCount(adoptedWrapper));

        LambdaQueryWrapper<CollectionOpinion> partialWrapper = new LambdaQueryWrapper<>();
        partialWrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .eq(CollectionOpinion::getStatus, "部分采纳");
        stats.put("partiallyAdopted", opinionMapper.selectCount(partialWrapper));

        LambdaQueryWrapper<CollectionOpinion> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .eq(CollectionOpinion::getStatus, "不采纳");
        stats.put("rejected", opinionMapper.selectCount(rejectedWrapper));
        return stats;
    }

    // ========== C端接口 ==========

    /**
     * C端-查询进行中的征集列表
     */
    public IPage<Collection> listPublic(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Collection::getTitle, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Collection::getStatus, status);
        }
        wrapper.orderByDesc(Collection::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * C端-征集详情
     */
    public Collection detail(Long id) {
        Collection c = getById(id);
        if (c == null) {
            throw BusinessException.of("征集不存在");
        }
        return c;
    }

    /**
     * C端-提交意见
     */
    public CollectionOpinion submitOpinion(CollectionOpinion opinion) {
        if (opinion.getCollectionId() == null) {
            throw BusinessException.of("征集ID不能为空");
        }
        Collection c = getById(opinion.getCollectionId());
        if (c == null) {
            throw BusinessException.of("征集不存在");
        }
        if (!StringUtils.hasText(opinion.getTitle())) {
            throw BusinessException.of("意见标题不能为空");
        }
        if (!StringUtils.hasText(opinion.getRealName())) {
            throw BusinessException.of("姓名不能为空");
        }
        if (!StringUtils.hasText(opinion.getPhone())) {
            throw BusinessException.of("手机号码不能为空");
        }
        if (!StringUtils.hasText(opinion.getContent())) {
            throw BusinessException.of("留言内容不能为空");
        }
        opinion.setStatus("待处理");
        opinionMapper.insert(opinion);
        return opinion;
    }

    /**
     * C端-查询某征集下的公开意见（已回复的）
     */
    public List<CollectionOpinion> listPublicOpinions(Long collectionId) {
        LambdaQueryWrapper<CollectionOpinion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionOpinion::getCollectionId, collectionId)
                .in(CollectionOpinion::getStatus, "已采纳", "部分采纳", "不采纳")
                .orderByDesc(CollectionOpinion::getCreateTime);
        return opinionMapper.selectList(wrapper);
    }
}
