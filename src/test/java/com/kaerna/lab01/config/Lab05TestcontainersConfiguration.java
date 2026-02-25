package com.kaerna.lab01.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;

@TestConfiguration(proxyBeanMethods = false)
@Profile("lab05")
public class Lab05TestcontainersConfiguration {
}
