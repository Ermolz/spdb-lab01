package com.kaerna.lab01;

import com.kaerna.lab01.redis.CacheableEntity;
import com.kaerna.lab01.repository.CacheableEntityRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class CacheableEntityRedisRepositoryTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestContainer.registerProperties(registry);
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    CacheableEntityRedisRepository repository;

    @Test
    void save_persistsEntity() {
        CacheableEntity entity = new CacheableEntity("c1", "Label1", "Value1");
        CacheableEntity saved = repository.save(entity);
        assertThat(saved.getId()).isEqualTo("c1");
    }

    @Test
    void findById_returnsSavedEntity() {
        CacheableEntity entity = new CacheableEntity("c2", "Label2", "Value2");
        repository.save(entity);

        assertThat(repository.findById("c2")).isPresent();
        assertThat(repository.findById("c2").get().getLabel()).isEqualTo("Label2");
        assertThat(repository.findById("c2").get().getValue()).isEqualTo("Value2");
    }

    @Test
    void deleteById_removesEntity() {
        CacheableEntity entity = new CacheableEntity("c3", "Label3", "Value3");
        repository.save(entity);
        assertThat(repository.existsById("c3")).isTrue();

        repository.deleteById("c3");
        assertThat(repository.existsById("c3")).isFalse();
        assertThat(repository.findById("c3")).isEmpty();
    }
}
