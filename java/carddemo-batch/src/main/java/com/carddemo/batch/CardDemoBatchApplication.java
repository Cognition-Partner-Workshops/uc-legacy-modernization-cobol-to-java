package com.carddemo.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Main Spring Boot application class for CardDemo batch processing.
 * Replaces JCL-driven COBOL batch programs (CBTRN02C, CBACT04C, CBSTM03A, etc.)
 */
@SpringBootApplication
@EnableBatchProcessing
@EntityScan(basePackages = "com.carddemo.common.model")
public class CardDemoBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoBatchApplication.class, args);
    }
}
