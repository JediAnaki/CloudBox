package com.example.zhukov.cloudbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CloudBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudBoxApplication.class, args);
    }

}
