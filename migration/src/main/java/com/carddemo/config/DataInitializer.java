package com.carddemo.config;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Load sample data from app/data/ASCII/ files.
 * The bulk of the data is loaded via schema.sql and data.sql.
 * This initializer handles any post-load processing like password encoding.
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initializeData(UserSecurityRepository userSecurityRepository,
                                             PasswordEncoder passwordEncoder) {
        return args -> {
            // Ensure default admin and user accounts exist with BCrypt-encoded passwords
            if (userSecurityRepository.count() == 0) {
                UserSecurity admin = new UserSecurity();
                admin.setUserId("ADMIN001");
                admin.setFirstName("System");
                admin.setLastName("Admin");
                admin.setPassword(passwordEncoder.encode("ADMIN001"));
                admin.setUserType("A");
                userSecurityRepository.save(admin);

                UserSecurity user = new UserSecurity();
                user.setUserId("USER0001");
                user.setFirstName("Default");
                user.setLastName("User");
                user.setPassword(passwordEncoder.encode("USER0001"));
                user.setUserType("U");
                userSecurityRepository.save(user);

                log.info("Default users created: ADMIN001 (admin), USER0001 (user)");
            } else {
                log.info("Users already loaded from data.sql, count: {}", userSecurityRepository.count());
            }
        };
    }
}
