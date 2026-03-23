package com.carddemo.integration;

import com.carddemo.entity.UserSecurity;
import com.carddemo.exception.InvalidCredentialsException;
import com.carddemo.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AuthenticationService.
 * Verifies COBOL COSGN00C.cbl sign-on logic against real H2 database.
 *
 * COBOL source: app/cbl/COSGN00C.cbl lines 209-257
 * Tests user lookup, password comparison, admin/user routing.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationIntegrationTest {

    @Autowired
    private AuthenticationService authService;

    @Test
    @DisplayName("Admin user authenticates and routes to admin menu (COSGN00C user type 'A')")
    void shouldAuthenticateAdminUser_andRouteToAdminMenu() {
        // ADMIN001 is loaded by data.sql with userType 'A' and password 'ADMIN001'
        UserSecurity user = authService.authenticate("ADMIN001", "ADMIN001");

        assertNotNull(user);
        assertEquals("ADMIN001", user.getUserId());
        assertTrue(user.isAdmin(), "User type should be admin");
        assertEquals("redirect:/admin/menu", authService.getLandingPage(user));
    }

    @Test
    @DisplayName("Regular user authenticates and routes to main menu (COSGN00C user type 'U')")
    void shouldAuthenticateRegularUser_andRouteToMainMenu() {
        // USER0001 is loaded by data.sql with userType 'U' and password 'USER0001'
        UserSecurity user = authService.authenticate("USER0001", "USER0001");

        assertNotNull(user);
        assertEquals("USER0001", user.getUserId());
        assertTrue(user.isUser(), "User type should be regular user");
        assertEquals("redirect:/menu", authService.getLandingPage(user));
    }

    @Test
    @DisplayName("Nonexistent user throws 'User not found' (COSGN00C lines 220-230)")
    void shouldThrow_whenUserNotFound() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate("NOUSER99", "PASSWORD"));

        assertTrue(ex.getMessage().contains("User not found"),
                "Error message should match COBOL behavior: 'User not found'");
    }

    @Test
    @DisplayName("Wrong password throws 'Wrong Password' (COSGN00C lines 240-250)")
    void shouldThrow_whenPasswordIsWrong() {
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.authenticate("ADMIN001", "WRONGPWD"));

        assertTrue(ex.getMessage().contains("Wrong Password"),
                "Error message should match COBOL behavior: 'Wrong Password'");
    }

    @Test
    @DisplayName("Case-insensitive userId lookup (COBOL uppercases input)")
    void shouldAuthenticateWithLowercaseUserId() {
        UserSecurity user = authService.authenticate("admin001", "ADMIN001");
        assertNotNull(user);
        assertEquals("ADMIN001", user.getUserId());
    }
}
