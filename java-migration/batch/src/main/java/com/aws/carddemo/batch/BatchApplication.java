package com.aws.carddemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aws.carddemo.batch")
@EntityScan("com.aws.carddemo.domain")
@EnableJpaRepositories("com.aws.carddemo.domain")
@Import(com.aws.carddemo.dataload.SeedDataLoader.class)
public class BatchApplication {
  public static void main(String[] args) {
    SpringApplication.run(BatchApplication.class, args);
  }
}
