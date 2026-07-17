package com.gov.interaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gov.common.result.PageResult;
import com.gov.common.result.Result;
import com.gov.interaction.entity.Questionnaire;
import com.gov.interaction.service.QuestionnaireService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C5-民意调查问卷
 */
@RestController
@RequestMapping("/api/v1/interaction/questionnaire")
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }

    /**
     * B端-创建问卷
     */
    @PostMapping
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        Questionnaire q = questionnaireService.create(body);
        return Result.success(Map.of("id", q.getId()), "问卷创建成功");
    }

    /**
     * B端-问卷列表
     */
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<Questionnaire> pageResult = questionnaireService.listPage(keyword, status, page, size);
        List<Map<String, Object>> records = pageResult.getRecords().stream()
                .map(this::toMap)
                .toList();
        PageResult<Map<String, Object>> result = new PageResult<>(
                records, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
        return Result.success(result);
    }

    /**
     * 问卷详情（含题目）
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(questionnaireService.detailWithQuestions(id));
    }

    /**
     * B端-发布问卷
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        questionnaireService.publish(id);
        return Result.success();
    }

    /**
     * B端-关闭问卷
     */
    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        questionnaireService.close(id);
        return Result.success();
    }

    /**
     * C端-填写问卷
     */
    @PostMapping("/{id}/submit")
    public Result<Map<String, Object>> submitAnswer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        questionnaireService.submitAnswer(id, body);
        return Result.success(null, "提交成功，感谢您的参与！");
    }

    /**
     * B端-统计结果
     */
    @GetMapping("/{id}/statistics")
    public Result<Map<String, Object>> statistics(@PathVariable Long id) {
        return Result.success(questionnaireService.statistics(id));
    }

    private Map<String, Object> toMap(Questionnaire q) {
        return Map.of(
                "id", q.getId() != null ? q.getId() : 0,
                "title", q.getTitle() != null ? q.getTitle() : "",
                "description", q.getDescription() != null ? q.getDescription() : "",
                "status", q.getStatus() != null ? q.getStatus() : "",
                "totalAnswers", q.getTotalAnswers() != null ? q.getTotalAnswers() : 0,
                "publishTime", q.getPublishTime() != null ? q.getPublishTime().toString() : "",
                "createTime", q.getCreateTime() != null ? q.getCreateTime().toString() : ""
        );
    }
}
