package com.carddemo.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.batch.JobExecutionExitCodeGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.carddemo.domain.entity")
@EnableJpaRepositories("com.carddemo.domain.repository")
public class CardDemoBatchApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CardDemoBatchApplication.class, args);
        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }

    @Bean
    JobExecutionExitCodeGenerator jobExecutionExitCodeGenerator() {
        return new JobExecutionExitCodeGenerator();
    }
}
