package com.carddemo.service;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;

    public AuthenticationService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    /**
     * Authenticate a user by user ID and password.
     * Mirrors COBOL COSGN00C.cbl READ-USER-SEC-FILE logic:
     * - Look up user by ID (upper-cased)
     * - Compare password
     * - Return user type (A=admin, U=user)
     */
    public UserSecurity authenticate(String userId, String password) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter User ID ...");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter Password ...");
        }

        String upperUserId = userId.toUpperCase().trim();
        String upperPassword = password.toUpperCase().trim();

        UserSecurity user = userSecurityRepository.findById(upperUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Try again ..."));

        if (!user.getPassword().equals(upperPassword)) {
            throw new IllegalArgumentException("Wrong Password. Try again ...");
        }

        return user;
    }
}
