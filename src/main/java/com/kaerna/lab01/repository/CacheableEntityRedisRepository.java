package com.kaerna.lab01.repository;

import com.kaerna.lab01.redis.CacheableEntity;
import org.springframework.data.repository.CrudRepository;

public interface CacheableEntityRedisRepository extends CrudRepository<CacheableEntity, String> {
}
