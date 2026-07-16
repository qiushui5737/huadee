package com.gov.service.controller;

import com.gov.common.result.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * B2-动态表单引擎 & B3-审批流程引擎
 */
@RestController
@RequestMapping("/api/v1/service/form")
public class FormController {

    @GetMapping("/schema/{itemId}")
    public Result<Map<String,Object>> formSchema(@PathVariable Long itemId) {
        // TODO: 返回JSON Schema表单结构
        return Result.success(Collections.emptyMap());
    }

    @PostMapping("/submit")
    public Result<Map<String,Object>> submit(@RequestBody Map<String,Object> formData) {
        // TODO: 1.表单校验 2.创建Flowable流程实例 3.返回受理号
        Map<String,Object> result = new HashMap<>();
        result.put("acceptNo", "SL20260715" + System.currentTimeMillis() % 10000);
        result.put("status", "受理中");
        return Result.success(result, "提交成功");
    }

    @GetMapping("/progress/{acceptNo}")
    public Result<List<Map<String,Object>>> progress(@PathVariable String acceptNo) {
        // TODO: 查询审批流程进度
        return Result.success(Collections.emptyList());
    }
}
