package com.carddemo.usersecurity.config;

import com.carddemo.usersecurity.domain.User;
import com.carddemo.usersecurity.repository.UserRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Loads the fixed-width USRSEC records shipped in app/jcl/ESDSRRDS.jcl using the
 * SEC-USER-DATA layout of copybook CSUSR01Y: user ID 1-8, first name 9-28,
 * last name 29-48, password 49-56, user type 57.
 */
@Component
public class UsrsecSeeder implements CommandLineRunner {

    private static final int USER_ID_END = 8;
    private static final int FIRST_NAME_END = 28;
    private static final int LAST_NAME_END = 48;
    private static final int PASSWORD_END = 56;
    private static final int USER_TYPE_END = 57;
    private static final String ADMIN_ID_PREFIX = "ADMIN";

    private final UserRepository userRepository;
    private final Resource seedData;

    public UsrsecSeeder(UserRepository userRepository,
                        @Value("classpath:usrsec-seed.txt") Resource seedData) {
        this.userRepository = userRepository;
        this.seedData = seedData;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(readSeedRecords());
    }

    List<User> readSeedRecords() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(seedData.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                users.add(parse(line));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to read USRSEC seed data", e);
        }
        return users;
    }

    static User parse(String record) {
        String padded = record.length() < USER_TYPE_END
                ? record + " ".repeat(USER_TYPE_END - record.length())
                : record;
        String userId = padded.substring(0, USER_ID_END).trim();
        String firstName = padded.substring(USER_ID_END, FIRST_NAME_END).trim();
        String lastName = padded.substring(FIRST_NAME_END, LAST_NAME_END).trim();
        String password = padded.substring(LAST_NAME_END, PASSWORD_END).trim();
        String userType = padded.substring(PASSWORD_END, USER_TYPE_END).trim();
        if (userType.isEmpty()) {
            userType = userId.startsWith(ADMIN_ID_PREFIX) ? "A" : "U";
        }
        return new User(userId, firstName, lastName, password, userType);
    }
}
