package com.carddemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the CardDemo Batch Java application.
 * Migrated from COBOL batch programs to Java 17+ with Spring Batch.
 */
@SpringBootApplication
public class CardDemoBatchApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(CardDemoBatchApplication.class, args)));
    }
}
