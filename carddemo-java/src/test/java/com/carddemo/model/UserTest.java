package com.carddemo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity.
 * Validates field mapping from CSUSR01Y.cpy SEC-USER-DATA layout.
 */
class UserTest {

    @Test
    void testUserCreation() {
        User user = new User();
        user.setUsrId("ADMIN001");
        user.setUsrFirstName("System");
        user.setUsrLastName("Admin");
        user.setUsrPwd("$2a$10$hashedpassword");
        user.setUsrType("A");

        assertEquals("ADMIN001", user.getUsrId());
        assertEquals("System", user.getUsrFirstName());
        assertEquals("Admin", user.getUsrLastName());
        assertEquals("$2a$10$hashedpassword", user.getUsrPwd());
        assertEquals("A", user.getUsrType());
    }

    @Test
    void testIsAdmin() {
        User admin = new User();
        admin.setUsrType("A");
        assertTrue(admin.isAdmin());

        User regular = new User();
        regular.setUsrType("U");
        assertFalse(regular.isAdmin());
    }

    @Test
    void testIsAdminCaseSensitive() {
        User user = new User();
        user.setUsrType("a");
        // COBOL EVALUATE is case-sensitive, so lowercase 'a' should NOT be admin
        assertFalse(user.isAdmin());
    }
}
