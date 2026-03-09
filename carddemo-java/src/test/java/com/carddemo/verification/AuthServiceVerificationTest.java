package com.carddemo.verification;

import com.carddemo.dto.LoginRequest;
import com.carddemo.dto.LoginResponse;
import com.carddemo.model.User;
import com.carddemo.repository.UserRepository;
import com.carddemo.service.online.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Strategy 3: Business Logic Unit Tests from COBOL - Authentication.
 *
 * Tests derived from COBOL program COSGN00C (Sign-On, CICS txn CC00).
 *
 * <p>Original COBOL flow (PROCESS-ENTER-KEY paragraph):
 * <ol>
 *   <li>READ USRSEC file using SEC-USR-ID as key</li>
 *   <li>If record not found: "Userid/Password is invalid. Please try again ..."</li>
 *   <li>If found, compare SEC-USR-PWD with input</li>
 *   <li>If mismatch: "Userid/Password is invalid. Please try again ..."</li>
 *   <li>If match: set COMMAREA (user-id, user-type, first/last name), XCTL to menu</li>
 *   <li>Admin users (SEC-USR-TYPE = 'A') go to COADM01C, regular users ('U') to COMEN01C</li>
 * </ol>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceVerificationTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, userRepository);
    }

    /**
     * COSGN00C: Valid admin login - SEC-USR-TYPE = 'A'.
     * COBOL would XCTL to COADM01C (admin menu).
     */
    @Test
    void testValidAdminLogin() {
        User adminUser = new User();
        adminUser.setUsrId("ADMIN001");
        adminUser.setUsrFirstName("ADMIN");
        adminUser.setUsrLastName("USER");
        adminUser.setUsrPwd("$2a$10$hashed");
        adminUser.setUsrType("A");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));

        LoginRequest request = new LoginRequest("admin001", "AdminPwd1");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("ADMIN001", response.getUserId());
        assertEquals("A", response.getUserType());
        assertEquals("ADMIN", response.getFirstName());
        assertEquals("USER", response.getLastName());
        assertEquals("Sign-on successful", response.getMessage());
    }

    /**
     * COSGN00C: Valid regular user login - SEC-USR-TYPE = 'U'.
     * COBOL would XCTL to COMEN01C (user menu).
     */
    @Test
    void testValidRegularUserLogin() {
        User regularUser = new User();
        regularUser.setUsrId("USER0001");
        regularUser.setUsrFirstName("FIRST");
        regularUser.setUsrLastName("LAST");
        regularUser.setUsrPwd("$2a$10$hashed");
        regularUser.setUsrType("U");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));

        LoginRequest request = new LoginRequest("user0001", "UserPwd01");
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("USER0001", response.getUserId());
        assertEquals("U", response.getUserType());
    }

    /**
     * COSGN00C: Invalid password - COBOL displays
     * "Userid/Password is invalid. Please try again ..."
     */
    @Test
    void testInvalidPasswordThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest("ADMIN001", "wrongpwd");

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
        assertTrue(ex.getMessage().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    /**
     * COSGN00C: User not found in USRSEC file.
     * COBOL READ with INVALID KEY gives same error as wrong password.
     */
    @Test
    void testNonExistentUserThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest("NOUSER99", "anypwd");

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));
    }

    /**
     * COSGN00C: User ID is uppercased before lookup.
     * COBOL stores USR-ID in uppercase; Java should normalize.
     */
    @Test
    void testUserIdIsUppercased() {
        User user = new User();
        user.setUsrId("USER0001");
        user.setUsrFirstName("FIRST");
        user.setUsrLastName("LAST");
        user.setUsrPwd("$2a$10$hashed");
        user.setUsrType("U");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById("USER0001")).thenReturn(Optional.of(user));

        // Lowercase input should be uppercased by AuthService
        LoginRequest request = new LoginRequest("user0001", "UserPwd01");
        LoginResponse response = authService.login(request);

        assertEquals("USER0001", response.getUserId());
    }

    /**
     * COSGN00C: Admin vs User routing.
     * Admin type 'A' routes differently than user type 'U'.
     */
    @Test
    void testUserTypeDistinction() {
        User admin = new User();
        admin.setUsrId("ADMIN001");
        admin.setUsrType("A");
        assertTrue(admin.isAdmin(), "Type 'A' should be admin");

        User regular = new User();
        regular.setUsrId("USER0001");
        regular.setUsrType("U");
        assertFalse(regular.isAdmin(), "Type 'U' should not be admin");
    }
}
