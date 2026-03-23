package com.carddemo.controller;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Authentication controller - replaces COBOL program COSGN00C (CC00 transaction).
 *
 * Legacy CICS flow:
 *   User enters credentials on BMS map COSGN0A
 *   -> COSGN00C reads USRSEC VSAM file
 *   -> Validates password
 *   -> XCTL to COADM01C (admin) or COMEN01C (user menu)
 *
 * Modernized: REST endpoint returns JWT-style token for subsequent API calls
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userService.authenticate(request);
    }
}
