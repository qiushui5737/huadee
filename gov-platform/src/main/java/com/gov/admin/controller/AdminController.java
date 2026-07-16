package com.gov.admin.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import com.gov.common.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * E3-绩效管理 & E4-认证权限 & E5-消息中心
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    // ===== E4-认证权限 =====
    @PostMapping("/auth/login")
    public Result<Map<String,Object>> login(@RequestBody Map<String,String> body) {
        String username = body.get("username");
        String password = body.get("password");
        // TODO: 校验用户名密码
        String token = JwtUtil.generateToken(1L, username, "ADMIN");
        Map<String,Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", username);
        result.put("role", "系统管理员");
        return Result.success(result, "登录成功");
    }

    @GetMapping("/auth/info")
    public Result<Map<String,Object>> userInfo(@RequestHeader("Authorization") String token) {
        // TODO: 从Token解析用户信息
        Map<String,Object> result = new HashMap<>();
        result.put("username", "管理员");
        result.put("roles", List.of("ADMIN"));
        result.put("permissions", List.of("*:*"));
        return Result.success(result);
    }

    // ===== E3-绩效管理 =====
    @GetMapping("/performance/ranking")
    public Result<List<Map<String,Object>>> performanceRanking() {
        // TODO: 部门绩效排名
        return Result.success(List.of(
            Map.of("dept","教育部","score",92.5,"rank",1),
            Map.of("dept","卫健委","score",89.3,"rank",2),
            Map.of("dept","人社局","score",87.1,"rank",3)
        ));
    }

    // ===== E5-消息中心 =====
    @GetMapping("/message/list")
    public Result<PageResult<Map<String,Object>>> messageList(
            @RequestParam(required=false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO: 消息列表(站内消息/短信/微信推送)
        return Result.success(PageResult.of(Collections.emptyList(), 0, page, size));
    }
}
