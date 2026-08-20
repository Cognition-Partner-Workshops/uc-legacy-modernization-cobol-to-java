package com.carddemo.web;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.carddemo.domain.entity")
@EnableJpaRepositories("com.carddemo.domain.repository")
public class CardDemoWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardDemoWebApplication.class, args);
    }
}
