package com.suitecrm.lead;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LeadServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeadServiceApplication.class, args);
    }
}
