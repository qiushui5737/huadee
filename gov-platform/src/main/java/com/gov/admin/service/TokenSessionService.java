package com.gov.admin.service;

import com.gov.common.utils.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenSessionService {
    private static final String TOKEN_KEY_PREFIX = "gov:auth:token:";
    private final StringRedisTemplate redisTemplate;

    public TokenSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String token, Long userId) {
        redisTemplate.opsForValue().set(
                key(token),
                userId.toString(),
                Duration.ofSeconds(JwtUtil.EXPIRATION_SECONDS)
        );
    }

    public boolean isValid(String token) {
        String userId = redisTemplate.opsForValue().get(key(token));
        return userId != null && userId.equals(JwtUtil.getUserId(token).toString());
    }

    public void remove(String token) {
        redisTemplate.delete(key(token));
    }

    private String key(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
