package com.kaerna.lab01.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final String KEY_PREFIX = "ratelimit:";

    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean tryAcquire(String userId, int maxRequests, Duration windowTtl) {
        String key = KEY_PREFIX + userId;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            return false;
        }
        if (count == 1) {
            stringRedisTemplate.expire(key, windowTtl);
        }
        return count <= maxRequests;
    }
}
