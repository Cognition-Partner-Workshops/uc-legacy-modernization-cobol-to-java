package com.carddemo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User(
                        "ADMIN001",
                        "ADMIN",
                        "USER",
                        passwordEncoder.encode("admin123"),
                        "A"
                ));

                userRepository.save(new User(
                        "USER0001",
                        "REGULAR",
                        "USER",
                        passwordEncoder.encode("user123"),
                        "U"
                ));
            }
        };
    }
}
