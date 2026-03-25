/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Application - Java migration of the COBOL mainframe CardDemo application.
 * Migrated from: COBOL/CICS/VSAM mainframe credit card management system.
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
