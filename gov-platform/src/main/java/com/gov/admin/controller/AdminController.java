package com.gov.admin.controller;

import com.gov.common.result.Result;
import com.gov.common.result.PageResult;
import com.gov.admin.service.AuthService;
import com.gov.admin.dto.RegisterRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * E3-绩效管理 & E4-认证权限 & E5-消息中心
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    // ===== E4-认证权限 =====
    @PostMapping("/auth/login")
    public Result<Map<String,Object>> login(@RequestBody Map<String,String> body) {
        return Result.success(authService.login(body.get("username"), body.get("password")), "登录成功");
    }

    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success(null, "注册成功，请登录");
    }

    @GetMapping("/auth/info")
    public Result<Map<String,Object>> userInfo(@RequestAttribute("jwtToken") String token) {
        return Result.success(authService.userInfo(token));
    }

    @PostMapping("/auth/logout")
    public Result<Void> logout(@RequestAttribute("jwtToken") String token) {
        authService.logout(token);
        return Result.success();
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
