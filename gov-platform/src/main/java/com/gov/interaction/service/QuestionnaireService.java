package com.gov.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gov.common.exception.BusinessException;
import com.gov.interaction.entity.*;
import com.gov.interaction.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * C5-问卷服务
 */
@Service
public class QuestionnaireService extends ServiceImpl<QuestionnaireMapper, Questionnaire> {

    private final QuestionnaireQuestionMapper questionMapper;
    private final QuestionnaireAnswerMapper answerMapper;
    private final QuestionnaireAnswerDetailMapper answerDetailMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuestionnaireService(QuestionnaireQuestionMapper questionMapper,
                                QuestionnaireAnswerMapper answerMapper,
                                QuestionnaireAnswerDetailMapper answerDetailMapper) {
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.answerDetailMapper = answerDetailMapper;
    }

    /**
     * 创建问卷（含题目）
     */
    @Transactional
    public Questionnaire create(Map<String, Object> body) {
        Questionnaire q = new Questionnaire();
        q.setTitle((String) body.get("title"));
        q.setDescription((String) body.get("description"));
        q.setStatus("草稿");
        q.setTotalAnswers(0);
        q.setCreateBy((String) body.getOrDefault("createBy", "管理员"));
        save(q);

        // 保存题目
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) body.get("questions");
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Map<String, Object> qm = questions.get(i);
                QuestionnaireQuestion qq = new QuestionnaireQuestion();
                qq.setQuestionnaireId(q.getId());
                qq.setQuestionText((String) qm.get("questionText"));
                qq.setQuestionType((String) qm.get("questionType"));
                qq.setRequired(Boolean.TRUE.equals(qm.get("required")));
                qq.setSortOrder(i);
                // 选项转JSON
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) qm.get("options");
                if (options != null) {
                    try {
                        qq.setOptions(objectMapper.writeValueAsString(options));
                    } catch (Exception e) {
                        qq.setOptions("[]");
                    }
                }
                questionMapper.insert(qq);
            }
        }
        return q;
    }

    /**
     * 发布问卷
     */
    public void publish(Long id) {
        Questionnaire q = getById(id);
        if (q == null) throw BusinessException.of("问卷不存在");
        q.setStatus("已发布");
        q.setPublishTime(LocalDateTime.now());
        updateById(q);
    }

    /**
     * 关闭问卷
     */
    public void close(Long id) {
        Questionnaire q = getById(id);
        if (q == null) throw BusinessException.of("问卷不存在");
        q.setStatus("已关闭");
        q.setCloseTime(LocalDateTime.now());
        updateById(q);
    }

    /**
     * 问卷详情（含题目）
     */
    public Map<String, Object> detailWithQuestions(Long id) {
        Questionnaire q = getById(id);
        if (q == null) throw BusinessException.of("问卷不存在");

        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, id)
                .orderByAsc(QuestionnaireQuestion::getSortOrder);
        List<QuestionnaireQuestion> questions = questionMapper.selectList(wrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", q.getId());
        result.put("title", q.getTitle());
        result.put("description", q.getDescription());
        result.put("status", q.getStatus());
        result.put("totalAnswers", q.getTotalAnswers());
        result.put("questions", questions.stream().map(this::questionToMap).collect(Collectors.toList()));
        return result;
    }

    /**
     * 问卷列表
     */
    public IPage<Questionnaire> listPage(String keyword, String status, int page, int size) {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Questionnaire::getTitle, keyword);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Questionnaire::getStatus, status);
        }
        wrapper.orderByDesc(Questionnaire::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * C端填写问卷
     */
    @Transactional
    public QuestionnaireAnswer submitAnswer(Long questionnaireId, Map<String, Object> body) {
        Questionnaire q = getById(questionnaireId);
        if (q == null) throw BusinessException.of("问卷不存在");
        if (!"已发布".equals(q.getStatus())) throw BusinessException.of("问卷未发布或已关闭");

        // 保存答卷
        QuestionnaireAnswer answer = new QuestionnaireAnswer();
        answer.setQuestionnaireId(questionnaireId);
        answer.setUserName((String) body.get("userName"));
        answer.setPhone((String) body.get("phone"));
        answer.setSubmitTime(LocalDateTime.now());
        answerMapper.insert(answer);

        // 保存答案详情
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        if (answers != null) {
            for (Map<String, Object> am : answers) {
                QuestionnaireAnswerDetail detail = new QuestionnaireAnswerDetail();
                detail.setAnswerId(answer.getId());
                detail.setQuestionId(((Number) am.get("questionId")).longValue());
                detail.setAnswerText((String) am.get("answerText"));
                @SuppressWarnings("unchecked")
                List<String> opts = (List<String>) am.get("answerOptions");
                if (opts != null) {
                    try {
                        detail.setAnswerOptions(objectMapper.writeValueAsString(opts));
                    } catch (Exception e) {
                        detail.setAnswerOptions("[]");
                    }
                }
                answerDetailMapper.insert(detail);
            }
        }

        // 更新答卷总数
        q.setTotalAnswers(q.getTotalAnswers() + 1);
        updateById(q);

        return answer;
    }

    /**
     * 统计结果
     */
    public Map<String, Object> statistics(Long id) {
        Questionnaire q = getById(id);
        if (q == null) throw BusinessException.of("问卷不存在");

        // 获取所有题目
        LambdaQueryWrapper<QuestionnaireQuestion> qWrapper = new LambdaQueryWrapper<>();
        qWrapper.eq(QuestionnaireQuestion::getQuestionnaireId, id)
                .orderByAsc(QuestionnaireQuestion::getSortOrder);
        List<QuestionnaireQuestion> questions = questionMapper.selectList(qWrapper);

        List<Map<String, Object>> questionStats = new ArrayList<>();
        for (QuestionnaireQuestion qq : questions) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("questionId", qq.getId());
            stat.put("questionText", qq.getQuestionText());
            stat.put("questionType", qq.getQuestionType());

            if ("text".equals(qq.getQuestionType())) {
                // 文本题：列出所有回答
                LambdaQueryWrapper<QuestionnaireAnswerDetail> dWrapper = new LambdaQueryWrapper<>();
                dWrapper.eq(QuestionnaireAnswerDetail::getQuestionId, qq.getId());
                List<QuestionnaireAnswerDetail> details = answerDetailMapper.selectList(dWrapper);
                stat.put("answers", details.stream().map(QuestionnaireAnswerDetail::getAnswerText).collect(Collectors.toList()));
            } else {
                // 选择题：统计各选项数量
                LambdaQueryWrapper<QuestionnaireAnswerDetail> dWrapper = new LambdaQueryWrapper<>();
                dWrapper.eq(QuestionnaireAnswerDetail::getQuestionId, qq.getId());
                List<QuestionnaireAnswerDetail> details = answerDetailMapper.selectList(dWrapper);

                Map<String, Integer> optionCount = new LinkedHashMap<>();
                // 解析选项
                try {
                    List<String> options = objectMapper.readValue(qq.getOptions(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    for (String opt : options) {
                        optionCount.put(opt, 0);
                    }
                } catch (Exception e) {
                    // ignore
                }
                // 统计
                for (QuestionnaireAnswerDetail detail : details) {
                    try {
                        List<String> selected = objectMapper.readValue(detail.getAnswerOptions(),
                                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                        for (String s : selected) {
                            optionCount.merge(s, 1, Integer::sum);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
                stat.put("optionStats", optionCount);
            }
            questionStats.add(stat);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionnaireId", q.getId());
        result.put("title", q.getTitle());
        result.put("totalAnswers", q.getTotalAnswers());
        result.put("questions", questionStats);
        return result;
    }

    private Map<String, Object> questionToMap(QuestionnaireQuestion qq) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", qq.getId());
        map.put("questionText", qq.getQuestionText());
        map.put("questionType", qq.getQuestionType());
        map.put("required", qq.getRequired());
        map.put("sortOrder", qq.getSortOrder());
        // 解析选项JSON
        try {
            List<String> options = objectMapper.readValue(qq.getOptions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            map.put("options", options);
        } catch (Exception e) {
            map.put("options", Collections.emptyList());
        }
        return map;
    }
}
