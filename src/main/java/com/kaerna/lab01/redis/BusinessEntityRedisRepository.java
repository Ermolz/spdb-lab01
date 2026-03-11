package com.kaerna.lab01.redis;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class BusinessEntityRedisRepository {

    private static final String KEY_PREFIX = "business:entity:";

    private final StringRedisTemplate stringRedisTemplate;
    private final HashOperations<String, String, String> hashOps;

    public BusinessEntityRedisRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.hashOps = stringRedisTemplate.opsForHash();
    }

    public void save(BusinessEntity entity) {
        String key = KEY_PREFIX + entity.getId();
        hashOps.putAll(key, entity.toMap());
    }

    public Optional<BusinessEntity> findById(String id) {
        String key = KEY_PREFIX + id;
        Map<String, String> entries = hashOps.entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BusinessEntity.fromMap(entries));
    }

    public void deleteById(String id) {
        stringRedisTemplate.delete(KEY_PREFIX + id);
    }

    public void updatePartial(String id, Map<String, String> patch) {
        String key = KEY_PREFIX + id;
        patch.forEach((field, value) -> hashOps.put(key, field, value));
    }
}
