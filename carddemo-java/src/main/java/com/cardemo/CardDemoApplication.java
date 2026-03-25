package com.cardemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Application - Modernized from COBOL/CICS mainframe application.
 * 
 * Original: CardDemo COBOL application with CICS online transactions
 * and batch processing programs for credit card management.
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
