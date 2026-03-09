package com.carddemo.controller;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.service.online.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller replacing COBOL COSGN00C (Sign-On, CICS txn CC00).
 * Maps BMS screen COSGN0A to REST endpoint POST /api/auth/login.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Sign-on operations (replaces COSGN00C / txn CC00)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User sign-on",
            description = "Authenticate user credentials. Replaces COBOL COSGN00C program. "
                    + "On success, returns user info and type (Admin/User) for menu routing.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
