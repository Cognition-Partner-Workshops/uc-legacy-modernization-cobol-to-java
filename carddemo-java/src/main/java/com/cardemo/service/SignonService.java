package com.cardemo.service;

import com.cardemo.model.SignonResult;
import com.cardemo.model.UserSecurity;
import com.cardemo.repository.UserSecurityRepository;
import java.util.Optional;

/**
 * Java equivalent of COBOL program COSGN00C.
 * Handles user sign-on/authentication for the CardDemo application.
 */
public class SignonService {

    private final UserSecurityRepository userSecurityRepository;

    public SignonService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    /**
     * Authenticates a user with the given credentials.
     * Equivalent of COBOL PROCESS-ENTER-KEY + READ-USER-SEC-FILE logic.
     *
     * @param userId   the user ID (will be uppercased)
     * @param password the password (will be uppercased)
     * @return SignonResult indicating success or failure with details
     */
    public SignonResult authenticate(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            return SignonResult.emptyUserId();
        }

        if (password == null || password.isBlank()) {
            return SignonResult.emptyPassword();
        }

        String normalizedUserId = userId.toUpperCase().trim();
        String normalizedPassword = password.toUpperCase().trim();

        try {
            Optional<UserSecurity> userOpt =
                    userSecurityRepository.findByUserId(normalizedUserId);

            if (userOpt.isEmpty()) {
                return SignonResult.userNotFound();
            }

            UserSecurity user = userOpt.get();

            if (!normalizedPassword.equals(user.getPassword())) {
                return SignonResult.wrongPassword();
            }

            return SignonResult.success(user);

        } catch (Exception e) {
            return SignonResult.systemError();
        }
    }

    /**
     * Determines the target program based on user type.
     * Admin users go to COADM01C, regular users go to COMEN01C.
     */
    public String getTargetProgram(UserSecurity user) {
        if (user == null) {
            return "COSGN00C";
        }
        return user.isAdmin() ? "COADM01C" : "COMEN01C";
    }
}
