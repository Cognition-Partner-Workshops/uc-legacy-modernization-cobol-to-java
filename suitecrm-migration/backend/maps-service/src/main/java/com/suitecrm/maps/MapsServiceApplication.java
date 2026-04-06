package com.suitecrm.maps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication @EnableDiscoveryClient
public class MapsServiceApplication {
    public static void main(String[] args) { SpringApplication.run(MapsServiceApplication.class, args); }
}
