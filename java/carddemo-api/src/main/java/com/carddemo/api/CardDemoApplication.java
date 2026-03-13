package com.carddemo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for the CardDemo API.
 * Replaces the CICS online transaction processing (COSGN00C, COMEN01C, etc.)
 */
@SpringBootApplication
@EntityScan(basePackages = "com.carddemo.common.model")
@EnableJpaRepositories(basePackages = "com.carddemo.api.repository")
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
