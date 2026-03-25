package com.cardemo.controller;

import com.cardemo.model.SecurityUser;
import com.cardemo.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller - converted from COBOL program COSGN00C.cbl
 * Original: CICS Signon Screen (Transaction CC00, Map COSGN0A)
 * Replaces BMS screen-based signon with REST endpoint.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/auth/login
     * Replaces CICS RECEIVE MAP('COSGN0A') / READ-USER-SEC-FILE logic.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String userId = credentials.get("userId");
        String password = credentials.get("password");

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter User ID ..."));
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter Password ..."));
        }

        Optional<SecurityUser> userOpt = authenticationService.authenticate(userId, password);
        if (userOpt.isPresent()) {
            SecurityUser user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "userId", user.getUserId(),
                    "firstName", user.getFirstName() != null ? user.getFirstName().trim() : "",
                    "lastName", user.getLastName() != null ? user.getLastName().trim() : "",
                    "userType", user.getUserType(),
                    "isAdmin", user.isAdmin(),
                    "message", "Login successful"
            ));
        } else {
            return ResponseEntity.status(401).body(
                    Map.of("error", "Invalid credentials. Try again ..."));
        }
    }
}
