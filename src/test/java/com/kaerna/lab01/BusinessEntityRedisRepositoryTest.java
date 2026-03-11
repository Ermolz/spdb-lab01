package com.kaerna.lab01;

import com.kaerna.lab01.redis.BusinessEntity;
import com.kaerna.lab01.redis.BusinessEntityRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class BusinessEntityRedisRepositoryTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestContainer.registerProperties(registry);
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    BusinessEntityRedisRepository repository;

    @Test
    void saveAndFindById_returnsSavedEntity() {
        BusinessEntity entity = new BusinessEntity("ent-1", "Acme", "vendor");
        repository.save(entity);

        Optional<BusinessEntity> found = repository.findById("ent-1");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Acme");
        assertThat(found.get().getType()).isEqualTo("vendor");
    }

    @Test
    void save_overwritesExisting() {
        repository.save(new BusinessEntity("ent-2", "Old", "typeA"));
        repository.save(new BusinessEntity("ent-2", "New", "typeB"));

        Optional<BusinessEntity> found = repository.findById("ent-2");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New");
        assertThat(found.get().getType()).isEqualTo("typeB");
    }

    @Test
    void deleteById_removesEntity() {
        repository.save(new BusinessEntity("ent-3", "Del", "x"));
        repository.deleteById("ent-3");
        assertThat(repository.findById("ent-3")).isEmpty();
    }

    @Test
    void updatePartial_updatesOnlyGivenFields() {
        repository.save(new BusinessEntity("ent-4", "Original", "type1"));
        repository.updatePartial("ent-4", Map.of("name", "Patched", "type", "type2"));

        Optional<BusinessEntity> found = repository.findById("ent-4");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Patched");
        assertThat(found.get().getType()).isEqualTo("type2");
    }

    @Test
    void updatePartial_onNonExistent_createsOnlyThoseFields() {
        repository.updatePartial("ent-5", Map.of("name", "OnlyName", "type", "OnlyType"));

        Optional<BusinessEntity> found = repository.findById("ent-5");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("OnlyName");
        assertThat(found.get().getType()).isEqualTo("OnlyType");
        assertThat(found.get().getId()).isNull();
    }
}
