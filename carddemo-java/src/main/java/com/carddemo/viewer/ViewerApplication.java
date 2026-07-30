package com.carddemo.viewer;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ViewerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ViewerApplication.class, args);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer statementJsonCustomizer() {
        return builder -> builder.featuresToEnable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
    }
}
