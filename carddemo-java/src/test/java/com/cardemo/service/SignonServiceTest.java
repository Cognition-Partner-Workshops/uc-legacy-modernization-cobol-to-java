package com.cardemo.service;

import com.cardemo.model.SignonResult;
import com.cardemo.model.UserSecurity;
import com.cardemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SignonService (COBOL COSGN00C equivalent).
 */
@ExtendWith(MockitoExtension.class)
class SignonServiceTest {

    @Mock
    private UserSecurityRepository userSecurityRepository;

    private SignonService signonService;

    @BeforeEach
    void setUp() {
        signonService = new SignonService(userSecurityRepository);
    }

    @Nested
    @DisplayName("authenticate()")
    class AuthenticateTests {

        @Test
        @DisplayName("Should return EMPTY_USER_ID when userId is null")
        void shouldRejectNullUserId() {
            SignonResult result = signonService.authenticate(null, "PASSWORD");

            assertEquals(SignonResult.Status.EMPTY_USER_ID, result.getStatus());
            assertEquals("Please enter User ID ...", result.getMessage());
            assertFalse(result.isSuccessful());
            assertNull(result.getUser());
            verifyNoInteractions(userSecurityRepository);
        }

        @Test
        @DisplayName("Should return EMPTY_USER_ID when userId is blank")
        void shouldRejectBlankUserId() {
            SignonResult result = signonService.authenticate("   ", "PASSWORD");

            assertEquals(SignonResult.Status.EMPTY_USER_ID, result.getStatus());
            assertFalse(result.isSuccessful());
        }

        @Test
        @DisplayName("Should return EMPTY_USER_ID when userId is empty string")
        void shouldRejectEmptyUserId() {
            SignonResult result = signonService.authenticate("", "PASSWORD");

            assertEquals(SignonResult.Status.EMPTY_USER_ID, result.getStatus());
        }

        @Test
        @DisplayName("Should return EMPTY_PASSWORD when password is null")
        void shouldRejectNullPassword() {
            SignonResult result = signonService.authenticate("USER0001", null);

            assertEquals(SignonResult.Status.EMPTY_PASSWORD, result.getStatus());
            assertEquals("Please enter Password ...", result.getMessage());
            assertFalse(result.isSuccessful());
        }

        @Test
        @DisplayName("Should return EMPTY_PASSWORD when password is blank")
        void shouldRejectBlankPassword() {
            SignonResult result = signonService.authenticate("USER0001", "  ");

            assertEquals(SignonResult.Status.EMPTY_PASSWORD, result.getStatus());
        }

        @Test
        @DisplayName("Should return USER_NOT_FOUND when user does not exist")
        void shouldReturnUserNotFound() {
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.empty());

            SignonResult result = signonService.authenticate("USER0001", "PASSWORD");

            assertEquals(SignonResult.Status.USER_NOT_FOUND, result.getStatus());
            assertEquals("User not found. Try again ...", result.getMessage());
            assertFalse(result.isSuccessful());
            assertNull(result.getUser());
        }

        @Test
        @DisplayName("Should return WRONG_PASSWORD when password doesn't match")
        void shouldReturnWrongPassword() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.of(user));

            SignonResult result = signonService.authenticate("USER0001", "WRONG");

            assertEquals(SignonResult.Status.WRONG_PASSWORD, result.getStatus());
            assertEquals("Wrong Password. Try again ...", result.getMessage());
            assertFalse(result.isSuccessful());
        }

        @Test
        @DisplayName("Should return SUCCESS for valid regular user credentials")
        void shouldAuthenticateRegularUser() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.of(user));

            SignonResult result = signonService.authenticate("USER0001", "PASSWORD");

            assertTrue(result.isSuccessful());
            assertEquals(SignonResult.Status.SUCCESS, result.getStatus());
            assertNotNull(result.getUser());
            assertEquals("USER0001", result.getUser().getUserId());
            assertFalse(result.getUser().isAdmin());
        }

        @Test
        @DisplayName("Should return SUCCESS for valid admin credentials")
        void shouldAuthenticateAdminUser() {
            UserSecurity admin = new UserSecurity("ADMIN001", "Admin", "User",
                    "PASSWORD", "A");
            when(userSecurityRepository.findByUserId("ADMIN001"))
                    .thenReturn(Optional.of(admin));

            SignonResult result = signonService.authenticate("ADMIN001", "PASSWORD");

            assertTrue(result.isSuccessful());
            assertNotNull(result.getUser());
            assertTrue(result.getUser().isAdmin());
        }

        @Test
        @DisplayName("Should uppercase userId before lookup")
        void shouldUppercaseUserId() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.of(user));

            SignonResult result = signonService.authenticate("user0001", "password");

            assertTrue(result.isSuccessful());
            verify(userSecurityRepository).findByUserId("USER0001");
        }

        @Test
        @DisplayName("Should uppercase password before comparison")
        void shouldUppercasePassword() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.of(user));

            SignonResult result = signonService.authenticate("USER0001", "password");

            assertTrue(result.isSuccessful());
        }

        @Test
        @DisplayName("Should trim userId and password")
        void shouldTrimInputs() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");
            when(userSecurityRepository.findByUserId("USER0001"))
                    .thenReturn(Optional.of(user));

            SignonResult result = signonService.authenticate("  USER0001  ", "  PASSWORD  ");

            assertTrue(result.isSuccessful());
        }

        @Test
        @DisplayName("Should return SYSTEM_ERROR when repository throws exception")
        void shouldHandleRepositoryException() {
            when(userSecurityRepository.findByUserId(anyString()))
                    .thenThrow(new RuntimeException("DB connection failed"));

            SignonResult result = signonService.authenticate("USER0001", "PASSWORD");

            assertEquals(SignonResult.Status.SYSTEM_ERROR, result.getStatus());
            assertEquals("Unable to verify the User ...", result.getMessage());
            assertFalse(result.isSuccessful());
        }
    }

    @Nested
    @DisplayName("getTargetProgram()")
    class GetTargetProgramTests {

        @Test
        @DisplayName("Should return admin menu for admin users")
        void shouldReturnAdminProgramForAdminUser() {
            UserSecurity admin = new UserSecurity("ADMIN001", "Admin", "User",
                    "PASSWORD", "A");

            String target = signonService.getTargetProgram(admin);

            assertEquals("COADM01C", target);
        }

        @Test
        @DisplayName("Should return main menu for regular users")
        void shouldReturnMainMenuForRegularUser() {
            UserSecurity user = new UserSecurity("USER0001", "John", "Doe",
                    "PASSWORD", "U");

            String target = signonService.getTargetProgram(user);

            assertEquals("COMEN01C", target);
        }

        @Test
        @DisplayName("Should return signon screen for null user")
        void shouldReturnSignonScreenForNullUser() {
            String target = signonService.getTargetProgram(null);

            assertEquals("COSGN00C", target);
        }
    }
}
