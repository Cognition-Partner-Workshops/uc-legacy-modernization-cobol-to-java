package com.cognition.usersecurity.config;

import com.cognition.usersecurity.model.User;
import com.cognition.usersecurity.model.UserType;
import com.cognition.usersecurity.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UserRepository repository) {
        return args -> {
            if (repository.count() > 0) return;
            repository.save(new User("ADMIN001", "MARGARET", "GOLD", "PASSWORDA", UserType.ADMIN));
            repository.save(new User("ADMIN002", "RUSSELL", "RUSSELL", "PASSWORDA", UserType.ADMIN));
            repository.save(new User("ADMIN003", "RAYMOND", "WHITMORE", "PASSWORDA", UserType.ADMIN));
            repository.save(new User("ADMIN004", "EMMANUEL", "CASGRAIN", "PASSWORDA", UserType.ADMIN));
            repository.save(new User("ADMIN005", "GRANVILLE", "LACHAPELLE", "PASSWORDA", UserType.ADMIN));
            repository.save(new User("USER0001", "LAWRENCE", "THOMAS", "PASSWORDU", UserType.USER));
            repository.save(new User("USER0002", "AJITH", "KUMAR", "PASSWORDU", UserType.USER));
            repository.save(new User("USER0003", "LAURITZ", "ALME", "PASSWORDU", UserType.USER));
            repository.save(new User("USER0004", "AVERARDO", "MAZZI", "PASSWORDU", UserType.USER));
            repository.save(new User("USER0005", "LEE", "TING", "PASSWORDU", UserType.USER));
        };
    }
}
