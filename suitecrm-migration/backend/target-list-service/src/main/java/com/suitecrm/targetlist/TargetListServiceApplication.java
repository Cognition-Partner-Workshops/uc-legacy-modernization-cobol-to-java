package com.suitecrm.targetlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TargetListServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TargetListServiceApplication.class, args);
    }
}
