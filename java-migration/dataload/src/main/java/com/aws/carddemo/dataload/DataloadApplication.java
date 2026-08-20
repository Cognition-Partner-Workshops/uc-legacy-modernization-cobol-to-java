package com.aws.carddemo.dataload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aws.carddemo")
@EntityScan("com.aws.carddemo.domain")
@EnableJpaRepositories("com.aws.carddemo.domain")
public class DataloadApplication {
  public static void main(String[] args) {
    SpringApplication.run(DataloadApplication.class, args);
  }
}
