package com.kaerna.lab01.config.lab05;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Profile("lab05")
public class Lab05DataSourceConfig {

    @Bean("primaryDataSource")
    @ConfigurationProperties(prefix = "app.datasource.primary")
    public HikariDataSource primaryDataSource() {
        return new HikariDataSource();
    }

    @Bean("replicaDataSourceRaw")
    @ConfigurationProperties(prefix = "app.datasource.replica")
    public HikariDataSource replicaDataSourceRaw() {
        return new HikariDataSource();
    }

    @Bean("replicaDataSource")
    public DataSource replicaDataSource(@Qualifier("primaryDataSource") DataSource primary) {
        return new FallbackDataSource(replicaDataSourceRaw(), primary);
    }

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica) {
        ReadWriteRoutingDataSource router = new ReadWriteRoutingDataSource();
        router.setDefaultTargetDataSource(primary);
        router.setTargetDataSources(Map.of(
                DataSourceType.PRIMARY, primary,
                DataSourceType.REPLICA, replica));
        router.afterPropertiesSet();
        return router;
    }
}
