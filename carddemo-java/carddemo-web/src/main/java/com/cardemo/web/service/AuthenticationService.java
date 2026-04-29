package com.cardemo.web.service;

import com.cardemo.common.repository.UserSecurityRepository;
import org.springframework.stereotype.Service;

/**
 * Authentication service replacing sign-on logic from COSGN00C.cbl.
 *
 * TODO: Implement credential validation against user_security table
 * TODO: Map to Spring Security UserDetailsService
 * TODO: Handle COBOL sign-on error messages (invalid user, wrong password, locked)
 */
@Service
public class AuthenticationService {

    private final UserSecurityRepository userSecurityRepository;

    public AuthenticationService(UserSecurityRepository userSecurityRepository) {
        this.userSecurityRepository = userSecurityRepository;
    }

    // TODO: Implement authentication logic from COSGN00C.cbl
}
