package com.carddemo.auth.controller;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.LoginResponse;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sign-on endpoint - the modern replacement for {@code COSGN00C}
 * (CICS transaction {@code CC00}), the entry point of the CardDemo application.
 *
 * <p>Legacy flow ({@code COSGN00C} {@code READ-USER-SEC-FILE}):
 * <ol>
 *   <li>Upper-cases the user ID and password.</li>
 *   <li>Reads {@code USRSEC} by {@code SEC-USR-ID}; "User not found" on RESP 13.</li>
 *   <li>Compares the <em>plaintext</em> {@code SEC-USR-PWD}; "Wrong Password" on mismatch.</li>
 *   <li>On success, stamps {@code CDEMO-USER-ID}/{@code CDEMO-USER-TYPE} into the
 *       COMMAREA and {@code XCTL}s to {@code COADM01C} (admin) or {@code COMEN01C} (user).</li>
 * </ol>
 *
 * <p>This endpoint preserves the user-ID upper-casing but replaces the plaintext
 * comparison with a BCrypt check and, instead of a COMMAREA hand-off, returns a
 * signed JWT carrying the identity claims. To avoid user enumeration, both
 * "user not found" and "wrong password" return a uniform {@code 401}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Authenticates a user and issues a JWT. Replaces {@code COSGN00C}.
     *
     * @param request the sign-on credentials
     * @return {@code 200} with a {@link LoginResponse} token on success
     * @throws ResponseStatusException {@code 401} on unknown user or bad password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Legacy COSGN00C upper-cases the user ID (FUNCTION UPPER-CASE).
        String userId = request.userId().toUpperCase();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getUserId(), user.getUserType());
        return ResponseEntity.ok(
                new LoginResponse(token, user.getUserId(), user.getUserType()));
    }
}
