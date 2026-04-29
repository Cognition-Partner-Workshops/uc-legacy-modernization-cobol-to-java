package com.cardemo.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application entry point for the CardDemo web module.
 * Replaces the CICS online transaction processing subsystem.
 */
@SpringBootApplication
@EntityScan(basePackages = "com.cardemo.common.entity")
@EnableJpaRepositories(basePackages = "com.cardemo.common.repository")
public class CardDemoWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoWebApplication.class, args);
    }
}
