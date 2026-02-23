package com.kaerna.lab01;

import org.flywaydb.core.Flyway;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

@TestConfiguration
public class FlywayFirstConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @DependsOn("dataSource")
    public FlywayRunner flywayRunner(DataSource dataSource) {
        return new FlywayRunner(dataSource);
    }

    public static class FlywayRunner {
        public FlywayRunner(DataSource dataSource) {
            Flyway.configure()
                    .dataSource(dataSource)
                    .load()
                    .migrate();
        }
    }
}
