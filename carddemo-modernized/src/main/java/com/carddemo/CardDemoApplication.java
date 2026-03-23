package com.carddemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo Modernized Application
 *
 * Reactive Java implementation of the legacy CardDemo mainframe COBOL application.
 * Replaces COBOL/CICS/VSAM with Spring Boot WebFlux, Reactive Cassandra, and JDBC.
 *
 * Original COBOL programs migrated:
 *   COSGN00C -> AuthController (Sign-on)
 *   COACTVWC/COACTUPC -> AccountController (Account View/Update)
 *   COCRDLIC/COCRDSLC/COCRDUPC -> CardController (Card List/View/Update)
 *   COTRN00C/COTRN01C/COTRN02C -> TransactionController (List/View/Add)
 *   COBIL00C -> BillPaymentController (Bill Payment)
 *   COUSR00C-COUSR03C -> UserController (Admin User Management)
 *   CORPT00C -> ReportController (Transaction Reports)
 */
@SpringBootApplication
public class CardDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardDemoApplication.class, args);
    }
}
