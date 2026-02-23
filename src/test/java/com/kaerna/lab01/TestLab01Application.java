package com.kaerna.lab01;

import org.springframework.boot.SpringApplication;

public class TestLab01Application {

    public static void main(String[] args) {
        SpringApplication.from(Lab01Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
