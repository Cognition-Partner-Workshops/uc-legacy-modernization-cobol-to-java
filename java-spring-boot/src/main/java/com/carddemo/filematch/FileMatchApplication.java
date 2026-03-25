package com.carddemo.filematch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CardDemo File Match Application.
 *
 * Java/Spring Boot equivalent of the COBOL batch programs CBACT01C and CBTRN02C
 * from the CardDemo mainframe application. Provides:
 * - CSV file reading/writing (replacing VSAM/sequential file I/O)
 * - H2 database persistence (replacing VSAM KSDS indexed files)
 * - REST API endpoints (replacing JCL batch job submission)
 */
@SpringBootApplication
public class FileMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileMatchApplication.class, args);
    }
}
