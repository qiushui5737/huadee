package com.gov.admin.service;

import com.gov.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenSessionService {
    private static final String TOKEN_KEY_PREFIX = "gov:auth:token:";
    private StringRedisTemplate redisTemplate;
    private final Map<String, String> memoryStorage = new ConcurrentHashMap<>();

    @Autowired(required = false)
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String token, Long userId) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(
                        key(token),
                        userId.toString(),
                        Duration.ofSeconds(JwtUtil.EXPIRATION_SECONDS)
                );
                return;
            } catch (Exception ignored) {
            }
        }
        memoryStorage.put(key(token), userId.toString());
    }

    public boolean isValid(String token) {
        if (redisTemplate != null) {
            try {
                String userId = redisTemplate.opsForValue().get(key(token));
                if (userId != null) {
                    return userId.equals(JwtUtil.getUserId(token).toString());
                }
            } catch (Exception ignored) {
            }
        }
        String userId = memoryStorage.get(key(token));
        return userId != null && userId.equals(JwtUtil.getUserId(token).toString());
    }

    public void remove(String token) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key(token));
                return;
            } catch (Exception ignored) {
            }
        }
        memoryStorage.remove(key(token));
    }

    private String key(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
