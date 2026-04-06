package com.suitecrm.kb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class KbServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KbServiceApplication.class, args);
    }
}
