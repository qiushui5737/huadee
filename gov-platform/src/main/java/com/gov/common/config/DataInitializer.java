package com.gov.common.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gov.admin.entity.SysUser;
import com.gov.admin.mapper.SysUserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        updatePasswordIfInvalid("admin");
        updatePasswordIfInvalid("edu_user");
        updatePasswordIfInvalid("hea_user");
        updatePasswordIfInvalid("pol_user");
        updatePasswordIfInvalid("soc_user");
        updatePasswordIfInvalid("med_user");
    }

    private void updatePasswordIfInvalid(String username) {
        SysUser user = userMapper.selectOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
        if (user != null) {
            String currentPassword = user.getPassword();
            if (currentPassword == null || currentPassword.length() < 60 || 
                !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode("Admin@123"));
                userMapper.updateById(user);
                System.out.println("Updated password for user: " + username);
            }
        }
    }
}