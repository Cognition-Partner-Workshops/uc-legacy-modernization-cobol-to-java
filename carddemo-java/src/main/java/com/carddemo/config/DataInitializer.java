package com.carddemo.config;

import com.carddemo.entity.User;
import com.carddemo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                String hashedPassword = passwordEncoder.encode("PASSWORD");

                userRepository.save(User.builder()
                        .userId("ADMIN001")
                        .firstName("ADMIN")
                        .lastName("USER")
                        .password(hashedPassword)
                        .userType("A")
                        .build());

                userRepository.save(User.builder()
                        .userId("USER0001")
                        .firstName("REGULAR")
                        .lastName("USER")
                        .password(hashedPassword)
                        .userType("U")
                        .build());
            }
        };
    }
}
