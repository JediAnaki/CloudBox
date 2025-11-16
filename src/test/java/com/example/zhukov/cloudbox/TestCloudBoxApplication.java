package com.example.zhukov.cloudbox;

import org.springframework.boot.SpringApplication;

public class TestCloudBoxApplication {

    public static void main(String[] args) {
        SpringApplication.from(CloudBoxApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
