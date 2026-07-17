package com.gov.admin.service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.admin.entity.SysUser;
import com.gov.admin.dto.RegisterRequest;
import com.gov.admin.mapper.SysUserMapper;
import com.gov.common.exception.BusinessException;
import com.gov.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);
        long count = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, request.getUsername().trim()));
        if (count > 0) throw BusinessException.of(409, "用户名已存在");

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setGender(request.getGender());
        user.setIdCard(request.getIdCard().trim().toUpperCase());
        user.setPhone(request.getPhone().trim());
        user.setEmail(request.getEmail().trim());
        user.setDeptCode(request.getDeptCode().trim().toUpperCase());
        user.setAddress(request.getAddress().trim());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        Long roleId = userMapper.selectRoleId("OPERATOR");
        if (roleId == null) throw BusinessException.of(500, "系统缺少默认角色配置");
        userMapper.insertUserRole(user.getId(), roleId);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getRealName()) || isBlank(request.getGender())
                || isBlank(request.getIdCard()) || isBlank(request.getPhone())
                || isBlank(request.getEmail()) || isBlank(request.getDeptCode())
                || isBlank(request.getAddress())) {
            throw BusinessException.of(400, "请完整填写注册信息");
        }
        if (!request.getUsername().matches("^[A-Za-z][A-Za-z0-9_]{4,19}$"))
            throw BusinessException.of(400, "用户名须为5-20位字母、数字或下划线，且以字母开头");
        if (request.getPassword().length() < 8
                || !request.getPassword().matches(".*[A-Za-z].*")
                || !request.getPassword().matches(".*[0-9].*"))
            throw BusinessException.of(400, "密码至少8位，且必须包含字母和数字");
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw BusinessException.of(400, "两次输入的密码不一致");
        if (!List.of("男", "女", "其他").contains(request.getGender()))
            throw BusinessException.of(400, "性别选项无效");
        if (!request.getIdCard().matches("^[0-9]{17}[0-9Xx]$"))
            throw BusinessException.of(400, "请输入有效的18位身份证号");
        if (!request.getPhone().matches("^1[0-9]{10}$"))
            throw BusinessException.of(400, "请输入有效的手机号");
        if (!request.getEmail().matches("^[^@ ]+@[^@ ]+[.][^@ ]+$"))
            throw BusinessException.of(400, "请输入有效的邮箱地址");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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

    @Transactional
    public Map<String, Object> updateProfile(String token, Map<String, String> body) {
        SysUser user = currentUser(token);
        String realName = body.get("realName");
        String gender = body.get("gender");
        String phone = body.get("phone");
        String email = body.get("email");
        String address = body.get("address");
        if (isBlank(realName) || isBlank(gender) || isBlank(phone) || isBlank(email) || isBlank(address))
            throw BusinessException.of(400, "请完整填写个人资料");
        if (!List.of("男", "女", "其他").contains(gender))
            throw BusinessException.of(400, "性别选项无效");
        if (!phone.matches("^1[0-9]{10}$"))
            throw BusinessException.of(400, "请输入有效的手机号");
        if (!email.matches("^[^@ ]+@[^@ ]+[.][^@ ]+$"))
            throw BusinessException.of(400, "请输入有效的邮箱地址");
        user.setRealName(realName.trim());
        user.setGender(gender);
        user.setPhone(phone.trim());
        user.setEmail(email.trim());
        user.setAddress(address.trim());
        userMapper.updateById(user);
        return userData(user, userMapper.selectRoleCodes(user.getId()));
    }

    @Transactional
    public void changePassword(String token, Map<String, String> body) {
        SysUser user = currentUser(token);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");
        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword))
            throw BusinessException.of(400, "请完整填写密码信息");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw BusinessException.of(400, "原密码不正确");
        if (newPassword.length() < 8
                || !newPassword.matches(".*[A-Za-z].*")
                || !newPassword.matches(".*[0-9].*"))
            throw BusinessException.of(400, "新密码至少8位，且必须包含字母和数字");
        if (!newPassword.equals(confirmPassword))
            throw BusinessException.of(400, "两次输入的新密码不一致");
        if (passwordEncoder.matches(newPassword, user.getPassword()))
            throw BusinessException.of(400, "新密码不能与原密码相同");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        tokenSessionService.remove(token);
    }

    private SysUser currentUser(String token) {
        Claims claims = JwtUtil.parseToken(token);
        Number userId = claims.get("userId", Number.class);
        SysUser user = userMapper.selectById(userId.longValue());
        if (user == null || !Integer.valueOf(1).equals(user.getStatus()))
            throw BusinessException.of(401, "登录状态已失效");
        return user;
    }

    private Map<String,Object> userData(SysUser user, List<String> roles) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("id", user.getId()); result.put("username", user.getUsername());
        result.put("realName", user.getRealName()); result.put("deptCode", user.getDeptCode());
        result.put("gender", user.getGender()); result.put("phone", user.getPhone());
        result.put("email", user.getEmail()); result.put("idCard", user.getIdCard());
        result.put("address", user.getAddress());
        result.put("roles", roles); result.put("permissions", roles.contains("ADMIN") ? List.of("*:*") : List.of());
        return result;
    }
}
