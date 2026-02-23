package com.kaerna.lab01;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Lab01ApplicationTests {

    @Test
    void contextLoads() {
    }

}
