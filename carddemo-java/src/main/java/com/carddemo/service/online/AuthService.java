package com.carddemo.service.online;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Authentication service replacing COBOL COSGN00C (Sign-On program, CICS txn CC00).
 *
 * <p>Original COBOL flow:
 * 1. SEND MAP COSGN0A (sign-on screen)
 * 2. RECEIVE MAP (get user ID + password)
 * 3. READ USRSEC file by user ID
 * 4. Compare password (plain-text)
 * 5. If match: set COMMAREA (user-id, user-type), XCTL to menu
 * 6. If no match: display error, allow retry
 *
 * <p>Java replacement uses Spring Security AuthenticationManager with BCrypt.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    /**
     * Authenticate user - replaces COSGN00C PROCESS-ENTER-KEY paragraph.
     */
    public LoginResponse login(LoginRequest request) {
        String userId = request.getUserId().toUpperCase();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userId, request.getPassword()));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            return new LoginResponse(
                    user.getUsrId(),
                    user.getUsrType(),
                    user.getUsrFirstName(),
                    user.getUsrLastName(),
                    "Sign-on successful"
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Userid/Password is invalid. Please try again ...");
        }
    }
}
