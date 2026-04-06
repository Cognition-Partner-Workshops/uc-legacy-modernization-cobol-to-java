package com.suitecrm.opportunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OpportunityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpportunityServiceApplication.class, args);
    }
}
