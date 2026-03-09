package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Java Application - Modernized from COBOL/CICS/VSAM mainframe application.
 *
 * <p>This application is a credit card management system originally built using
 * COBOL (online CICS programs + batch JCL jobs), VSAM KSDS data storage, and
 * BMS screen maps. It has been modernized to use Spring Boot, JPA/PostgreSQL,
 * Spring Security, Spring Batch, and REST APIs.</p>
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
