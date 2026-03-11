package com.kaerna.lab01.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserProfileRedisRepository {

    private static final String KEY_PREFIX = "profile:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public UserProfileRedisRepository(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(UserProfile profile) {
        String key = KEY_PREFIX + profile.getId();
        String json = toJson(profile);
        stringRedisTemplate.opsForValue().set(key, json);
    }

    public Optional<UserProfile> findById(String userId) {
        String key = KEY_PREFIX + userId;
        String json = stringRedisTemplate.opsForValue().get(key);
        return json == null ? Optional.empty() : Optional.of(fromJson(json));
    }

    public void deleteById(String userId) {
        stringRedisTemplate.delete(KEY_PREFIX + userId);
    }

    public boolean existsById(String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(KEY_PREFIX + userId));
    }

    private String toJson(UserProfile profile) {
        try {
            return objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize UserProfile", e);
        }
    }

    private UserProfile fromJson(String json) {
        try {
            return objectMapper.readValue(json, UserProfile.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize UserProfile", e);
        }
    }
}
