package com.gov.portal.controller;

import com.gov.common.result.Result;
import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.common.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/portal")
public class PortalController {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PortalController(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/auth/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String realName = body.get("realName");
        String phone = body.get("phone");

        if (username == null || username.isBlank()) {
            return Result.error("请输入用户名");
        }
        if (password == null || password.isBlank()) {
            return Result.error("请输入密码");
        }
        if (realName == null || realName.isBlank()) {
            return Result.error("请输入真实姓名");
        }

        long count = userMapper.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username.trim()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName.trim());
        user.setPhone(phone != null ? phone.trim() : "");
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        return Result.success(result, "注册成功，请登录");
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.error("请输入用户名和密码");
        }

        SysUser user = userMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username.trim()));

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        if (!Integer.valueOf(1).equals(user.getStatus())) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        List<String> roles = userMapper.selectRoleCodes(user.getId());
        if (roles == null || roles.isEmpty()) {
            roles = java.util.Collections.singletonList("USER");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("phone", user.getPhone());
        result.put("roles", roles);
        result.put("token", JwtUtil.generateToken(user.getId(), user.getUsername(), roles.get(0)));
        result.put("expiresIn", JwtUtil.EXPIRATION_SECONDS);

        return Result.success(result, "登录成功");
    }

    @GetMapping("/auth/info")
    public Result<Map<String, Object>> userInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("请先登录");
        }

        String token = authHeader.substring(7);
        try {
            var claims = JwtUtil.parseToken(token);
            Number userId = claims.get("userId", Number.class);
            SysUser user = userMapper.selectById(userId.longValue());

            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                return Result.error("登录状态已失效");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("phone", user.getPhone());
            result.put("roles", userMapper.selectRoleCodes(user.getId()));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("登录状态已失效");
        }
    }
}
