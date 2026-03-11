package com.kaerna.lab01;

import com.kaerna.lab01.redis.UserProfile;
import com.kaerna.lab01.redis.UserProfileRedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class UserProfileRedisRepositoryTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestContainer.registerProperties(registry);
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    UserProfileRedisRepository repository;

    @Test
    void saveAndFindById_returnsSavedProfile() {
        String userId = "user-1";
        UserProfile profile = new UserProfile(userId, "Alice", "alice@example.com");
        repository.save(profile);

        Optional<UserProfile> found = repository.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Alice");
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void save_overwritesExistingProfile() {
        String userId = "user-2";
        repository.save(new UserProfile(userId, "Bob", "bob@example.com"));
        repository.save(new UserProfile(userId, "Robert", "robert@example.com"));

        Optional<UserProfile> found = repository.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo("Robert");
    }

    @Test
    void deleteById_removesProfile() {
        String userId = "user-3";
        repository.save(new UserProfile(userId, "Carol", "carol@example.com"));
        assertThat(repository.existsById(userId)).isTrue();

        repository.deleteById(userId);
        assertThat(repository.findById(userId)).isEmpty();
        assertThat(repository.existsById(userId)).isFalse();
    }
}
