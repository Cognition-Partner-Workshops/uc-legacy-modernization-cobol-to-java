package com.carddemo.integration;

import com.carddemo.dto.UserRequest;
import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.UserManagementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for UserManagementService.
 * Verifies COBOL COUSR00C-03C user CRUD logic against real H2 database.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserManagementIntegrationTest {

    @Autowired
    private UserManagementService userService;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @Test
    @DisplayName("List all users (COUSR00C)")
    void shouldListAllUsers() {
        List<UserSecurity> users = userService.getAllUsers();

        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should have users from data.sql");
        assertTrue(users.stream().anyMatch(u -> "ADMIN001".equals(u.getUserId())));
        assertTrue(users.stream().anyMatch(u -> "USER0001".equals(u.getUserId())));
    }

    @Test
    @DisplayName("Add new user (COUSR01C)")
    void shouldAddNewUser() {
        UserRequest request = new UserRequest();
        request.setUserId("NEWUSR01");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("TESTPWD1");
        request.setUserType("U");

        UserSecurity created = userService.addUser(request);

        assertNotNull(created);
        assertEquals("NEWUSR01", created.getUserId());
        assertEquals("John", created.getFirstName());
        assertEquals("Doe", created.getLastName());
        assertEquals("U", created.getUserType());
        // Password should be BCrypt encoded
        assertTrue(created.getPassword().startsWith("$2a$") || created.getPassword().startsWith("$2b$"),
                "New user password should be BCrypt encoded");
    }

    @Test
    @DisplayName("Add duplicate user throws exception (COUSR01C)")
    void shouldThrow_whenAddingDuplicateUser() {
        UserRequest request = new UserRequest();
        request.setUserId("ADMIN001");
        request.setFirstName("Dup");
        request.setLastName("User");
        request.setPassword("PASSWORD");
        request.setUserType("A");

        assertThrows(RuntimeException.class, () -> userService.addUser(request));
    }

    @Test
    @DisplayName("Update user fields (COUSR02C)")
    void shouldUpdateUserFields() {
        UserRequest request = new UserRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        UserSecurity updated = userService.updateUser("USER0001", request);

        assertEquals("Updated", updated.getFirstName());
        assertEquals("Name", updated.getLastName());

        // Verify persisted
        UserSecurity reread = userService.getUser("USER0001");
        assertEquals("Updated", reread.getFirstName());
    }

    @Test
    @DisplayName("Delete user (COUSR03C)")
    void shouldDeleteUser() {
        // First add a user to delete
        UserRequest request = new UserRequest();
        request.setUserId("DELUSR01");
        request.setFirstName("Delete");
        request.setLastName("Me");
        request.setPassword("PASSWORD");
        request.setUserType("U");
        userService.addUser(request);

        assertTrue(userSecurityRepository.existsById("DELUSR01"));

        userService.deleteUser("DELUSR01");

        assertFalse(userSecurityRepository.existsById("DELUSR01"),
                "User should be deleted from database");
    }
}
