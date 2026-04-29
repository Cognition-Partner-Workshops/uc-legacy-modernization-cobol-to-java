package com.cardemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application entry point for the CardDemo batch module.
 * Replaces JCL batch job execution via JES2/Control-M.
 */
@SpringBootApplication
@EntityScan(basePackages = "com.cardemo.common.entity")
@EnableJpaRepositories(basePackages = "com.cardemo.common.repository")
public class CardDemoBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoBatchApplication.class, args);
    }
}
