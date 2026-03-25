package com.carddemo.api.service;

import com.carddemo.common.dto.LoginRequest;
import com.carddemo.common.dto.LoginResponse;
import org.springframework.stereotype.Service;

/**
 * Authentication service replacing COBOL program COSGN00C.
 * Handles user login validation against the USRSEC (User Security) VSAM file.
 *
 * Original COBOL: app/cbl/COSGN00C.cbl
 * CICS Transaction: CC00
 */
@Service
public class AuthenticationService {

    /**
     * Authenticate a user by userId and password.
     * Replaces the COSGN00C READ of USRSEC file and password comparison.
     */
    public LoginResponse login(LoginRequest request) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COSGN00C");
    }

    /**
     * Validate an existing session/token.
     */
    public boolean validateSession(String token) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COSGN00C");
    }
}
