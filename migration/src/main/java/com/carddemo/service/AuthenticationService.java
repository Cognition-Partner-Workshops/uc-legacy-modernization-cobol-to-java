package com.carddemo.service;

import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.InvalidCredentialsException;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Replaces COSGN00C.cbl - Sign-on screen logic.
 * Authenticates users and determines routing based on user type.
 */
@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserSecurityRepository userSecurityRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userSecurityRepository = userSecurityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate user by userId and password.
     * Matches COBOL logic from COSGN00C.cbl lines 209-257:
     * - Read user by userId from USRSEC file
     * - Compare password
     * - Route based on user type (A=admin, U=user)
     */
    public UserSecurity authenticate(String userId, String rawPassword) {
        String upperUserId = userId.toUpperCase().trim();
        String upperPassword = rawPassword.toUpperCase().trim();

        Optional<UserSecurity> userOpt = userSecurityRepository.findById(upperUserId);

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found. Try again ...");
        }

        UserSecurity user = userOpt.get();

        // Support both BCrypt and legacy plain-text passwords
        boolean passwordMatches;
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
            passwordMatches = passwordEncoder.matches(upperPassword, user.getPassword());
        } else {
            // Legacy plain-text comparison (COBOL behavior)
            passwordMatches = user.getPassword().trim().equals(upperPassword);
        }

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Wrong Password. Try again ...");
        }

        return user;
    }

    /**
     * Determine the landing page based on user type.
     * Admin users (type 'A') go to admin menu (COADM01C).
     * Regular users (type 'U') go to main menu (COMEN01C).
     */
    public String getLandingPage(UserSecurity user) {
        if (user.isAdmin()) {
            return "redirect:/admin/menu";
        }
        return "redirect:/menu";
    }
}
