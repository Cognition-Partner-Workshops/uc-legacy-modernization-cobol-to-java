package com.cardemo.service;

import com.cardemo.model.SecurityUser;
import com.cardemo.repository.SecurityUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service - converted from COBOL program COSGN00C.cbl
 * Original: CICS Signon Screen (Transaction CC00)
 * Handles user authentication against the security user file (USRSEC VSAM).
 */
@Service
public class AuthenticationService {

    private final SecurityUserRepository securityUserRepository;

    public AuthenticationService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    /**
     * Authenticate a user - equivalent to READ-USER-SEC-FILE paragraph in COSGN00C.
     *
     * @param userId   the user ID (up to 8 chars, case-insensitive)
     * @param password the password (up to 8 chars, case-insensitive)
     * @return the authenticated SecurityUser, or empty if authentication fails
     */
    public Optional<SecurityUser> authenticate(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }

        String upperUserId = userId.toUpperCase().trim();
        String upperPassword = password.toUpperCase().trim();

        Optional<SecurityUser> userOpt = securityUserRepository.findById(upperUserId);
        if (userOpt.isPresent()) {
            SecurityUser user = userOpt.get();
            if (user.getPassword() != null && user.getPassword().trim().equals(upperPassword)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
