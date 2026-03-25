package com.carddemo.api.controller;

import com.carddemo.common.dto.LoginRequest;
import com.carddemo.common.dto.LoginResponse;
import com.carddemo.api.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller replacing COBOL program COSGN00C (CC00 transaction).
 * Handles user sign-on and sign-off for the CardDemo application.
 *
 * Original COBOL: app/cbl/COSGN00C.cbl
 * CICS Transaction: CC00
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/auth/login — Authenticate user.
     * Replaces COSGN00C sign-on screen processing.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COSGN00C");
    }

    /**
     * POST /api/auth/logout — Invalidate session.
     * Replaces COSGN00C sign-off processing.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COSGN00C");
    }
}
