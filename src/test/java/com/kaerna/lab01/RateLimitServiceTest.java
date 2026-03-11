package com.kaerna.lab01;

import com.kaerna.lab01.redis.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedisTestConfiguration.class)
class RateLimitServiceTest {

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        RedisTestContainer.registerProperties(registry);
        MongoAndPostgresTestContainers.registerProperties(registry);
    }

    @Autowired
    RateLimitService rateLimitService;

    @Test
    void upToMaxRequests_allowed() {
        String userId = "rate-user-1";
        int max = 3;
        Duration ttl = Duration.ofSeconds(60);

        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
    }

    @Test
    void overMaxRequests_rejected() {
        String userId = "rate-user-2";
        int max = 2;
        Duration ttl = Duration.ofSeconds(60);

        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isFalse();
    }

    @Test
    void afterTtl_counterResets() throws InterruptedException {
        String userId = "rate-user-3";
        int max = 1;
        Duration ttl = Duration.ofSeconds(1);

        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isFalse();
        Thread.sleep(1100);
        assertThat(rateLimitService.tryAcquire(userId, max, ttl)).isTrue();
    }
}
