package com.suitecrm.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
public class SchedulerServiceApplication {
    public static void main(String[] args) { SpringApplication.run(SchedulerServiceApplication.class, args); }
}
