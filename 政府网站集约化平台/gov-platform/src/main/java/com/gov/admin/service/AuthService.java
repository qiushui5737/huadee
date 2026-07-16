package com.gov.admin.service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.common.exception.BusinessException;
import com.gov.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service
public class AuthService {
    private final SysUserMapper userMapper;
    private final TokenSessionService tokenSessionService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public AuthService(SysUserMapper userMapper, TokenSessionService tokenSessionService) {
        this.userMapper = userMapper;
        this.tokenSessionService = tokenSessionService;
    }
    public Map<String,Object> login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw BusinessException.of(400, "请输入用户名和密码");
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username.trim()));
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            throw BusinessException.of(401, "用户名或密码错误");
        if (!Integer.valueOf(1).equals(user.getStatus()))
            throw BusinessException.of(403, "账号已被禁用，请联系管理员");
        List<String> roles = userMapper.selectRoleCodes(user.getId());
        Map<String,Object> result = userData(user, roles);
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), roles.isEmpty() ? "OPERATOR" : roles.get(0));
        tokenSessionService.save(token, user.getId());
        result.put("token", token);
        result.put("expiresIn", JwtUtil.EXPIRATION_SECONDS);
        return result;
    }
    public void logout(String token) {
        tokenSessionService.remove(token);
    }
    public Map<String,Object> userInfo(String token) {
        Claims claims = JwtUtil.parseToken(token);
        Number userId = claims.get("userId", Number.class);
        SysUser user = userMapper.selectById(userId.longValue());
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
            throw BusinessException.of(401, "登录状态已失效");
        return userData(user, userMapper.selectRoleCodes(user.getId()));
    }
    private Map<String,Object> userData(SysUser user, List<String> roles) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("id", user.getId()); result.put("username", user.getUsername());
        result.put("realName", user.getRealName()); result.put("deptCode", user.getDeptCode());
        result.put("roles", roles); result.put("permissions", roles.contains("ADMIN") ? List.of("*:*") : List.of());
        return result;
    }
}
