package com.carddemo.statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.carddemo.statement", "com.carddemo.common"})
public class StatementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatementServiceApplication.class, args);
    }
}
