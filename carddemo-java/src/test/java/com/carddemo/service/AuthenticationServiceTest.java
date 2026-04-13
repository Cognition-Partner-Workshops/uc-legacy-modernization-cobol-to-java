package com.carddemo.service;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserSecurity adminUser;
    private UserSecurity regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new UserSecurity();
        adminUser.setUserId("ADMIN001");
        adminUser.setPassword("ADMIN001");
        adminUser.setUserType("A");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");

        regularUser = new UserSecurity();
        regularUser.setUserId("USER0001");
        regularUser.setPassword("USER0001");
        regularUser.setUserType("U");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
    }

    @Test
    void authenticate_validAdminLogin_returnsAdminUser() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));

        UserSecurity result = authenticationService.authenticate("admin001", "admin001");

        assertNotNull(result);
        assertEquals("ADMIN001", result.getUserId());
        assertEquals("A", result.getUserType());
        assertTrue(result.isAdmin());
        verify(userSecurityRepository).findById("ADMIN001");
    }

    @Test
    void authenticate_validRegularLogin_returnsRegularUser() {
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));

        UserSecurity result = authenticationService.authenticate("user0001", "user0001");

        assertNotNull(result);
        assertEquals("USER0001", result.getUserId());
        assertEquals("U", result.getUserType());
        assertFalse(result.isAdmin());
    }

    @Test
    void authenticate_invalidUser_throwsException() {
        when(userSecurityRepository.findById("INVALID")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("invalid", "pass"));
        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void authenticate_wrongPassword_throwsException() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("admin001", "wrongpass"));
        assertTrue(ex.getMessage().contains("Wrong Password"));
    }

    @Test
    void authenticate_emptyUserId_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("", "password"));
        assertTrue(ex.getMessage().contains("User ID"));
    }

    @Test
    void authenticate_emptyPassword_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("user", ""));
        assertTrue(ex.getMessage().contains("Password"));
    }

    @Test
    void authenticate_adminVsUserTypeDistinction() {
        when(userSecurityRepository.findById("ADMIN001")).thenReturn(Optional.of(adminUser));
        when(userSecurityRepository.findById("USER0001")).thenReturn(Optional.of(regularUser));

        UserSecurity admin = authenticationService.authenticate("ADMIN001", "ADMIN001");
        UserSecurity user = authenticationService.authenticate("USER0001", "USER0001");

        assertEquals("A", admin.getUserType());
        assertTrue(admin.isAdmin());
        assertEquals("U", user.getUserType());
        assertFalse(user.isAdmin());
    }
}
