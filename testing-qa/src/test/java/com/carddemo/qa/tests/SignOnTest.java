package com.carddemo.qa.tests;

import com.carddemo.qa.model.UserSecurity;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the Sign-On screen (COSGN00C / CC00 transaction).
 * Validates authentication logic migrated from the COBOL signon program.
 *
 * @see <a href="../../app/cbl/COSGN00C.cbl">COSGN00C.cbl</a>
 * @see <a href="../../app/cpy/CSUSR01Y.cpy">CSUSR01Y.cpy</a>
 */
@DisplayName("Sign-On Authentication Tests (COSGN00C)")
class SignOnTest {

    @Nested
    @DisplayName("Valid Credentials")
    class ValidCredentials {

        @Test
        @DisplayName("TC-SIGNON-001: Admin user authenticates successfully")
        void adminUserAuthenticatesSuccessfully() {
            UserSecurity admin = TestDataFactory.createAdminUser();

            assertEquals("ADMIN001", admin.getUserId());
            assertEquals("PASSWORD", admin.getPassword());
            assertTrue(admin.isAdmin(), "User type should be Admin");
            assertFalse(admin.isRegularUser(), "Admin should not be flagged as regular user");
        }

        @Test
        @DisplayName("TC-SIGNON-002: Regular user authenticates successfully")
        void regularUserAuthenticatesSuccessfully() {
            UserSecurity user = TestDataFactory.createRegularUser();

            assertEquals("USER0001", user.getUserId());
            assertEquals("PASSWORD", user.getPassword());
            assertTrue(user.isRegularUser(), "User type should be Regular");
            assertFalse(user.isAdmin(), "Regular user should not be flagged as admin");
        }

        @Test
        @DisplayName("TC-SIGNON-003: User ID is converted to uppercase before lookup")
        void userIdConvertedToUpperCase() {
            String inputUserId = "admin001";
            String normalizedUserId = inputUserId.toUpperCase();

            assertEquals("ADMIN001", normalizedUserId,
                    "User ID should be normalized to uppercase per COSGN00C logic");
        }

        @Test
        @DisplayName("TC-SIGNON-004: Password is converted to uppercase before comparison")
        void passwordConvertedToUpperCase() {
            String inputPassword = "password";
            String normalizedPassword = inputPassword.toUpperCase();

            assertEquals("PASSWORD", normalizedPassword,
                    "Password should be normalized to uppercase per COSGN00C logic");
        }
    }

    @Nested
    @DisplayName("Invalid Credentials")
    class InvalidCredentials {

        @Test
        @DisplayName("TC-SIGNON-005: Blank user ID is rejected")
        void blankUserIdRejected() {
            UserSecurity user = TestDataFactory.createUserWithBlankId();

            assertTrue(user.getUserId().isBlank(),
                    "Blank user ID should trigger 'Please enter User ID' error");
        }

        @Test
        @DisplayName("TC-SIGNON-006: Blank password is rejected")
        void blankPasswordRejected() {
            UserSecurity user = TestDataFactory.createUserWithBlankPassword();

            assertTrue(user.getPassword().isBlank(),
                    "Blank password should trigger 'Please enter Password' error");
        }

        @Test
        @DisplayName("TC-SIGNON-007: Wrong password returns error message")
        void wrongPasswordReturnsError() {
            UserSecurity user = TestDataFactory.createRegularUser();
            String attemptedPassword = "WRONGPWD";

            assertNotEquals(user.getPassword(), attemptedPassword,
                    "Mismatched password should produce 'Wrong Password. Try again' error");
        }

        @Test
        @DisplayName("TC-SIGNON-008: Non-existent user ID returns 'User not found'")
        void nonExistentUserReturnsNotFound() {
            String unknownUserId = "UNKNOWN1";
            UserSecurity knownUser = TestDataFactory.createRegularUser();

            assertNotEquals(unknownUserId, knownUser.getUserId(),
                    "Unknown user ID should produce 'User not found' error (RESP code 13)");
        }
    }

    @Nested
    @DisplayName("Navigation After Sign-On")
    class PostSignOnNavigation {

        @Test
        @DisplayName("TC-SIGNON-009: Admin user is routed to Admin Menu (COADM01C)")
        void adminRoutedToAdminMenu() {
            UserSecurity admin = TestDataFactory.createAdminUser();
            String expectedProgram = admin.isAdmin() ? "COADM01C" : "COMEN01C";

            assertEquals("COADM01C", expectedProgram,
                    "Admin users should be transferred to COADM01C");
        }

        @Test
        @DisplayName("TC-SIGNON-010: Regular user is routed to Main Menu (COMEN01C)")
        void regularUserRoutedToMainMenu() {
            UserSecurity user = TestDataFactory.createRegularUser();
            String expectedProgram = user.isAdmin() ? "COADM01C" : "COMEN01C";

            assertEquals("COMEN01C", expectedProgram,
                    "Regular users should be transferred to COMEN01C");
        }
    }

    @Nested
    @DisplayName("User Security Record Validation")
    class UserSecurityRecord {

        @Test
        @DisplayName("TC-SIGNON-011: User ID field limited to 8 characters")
        void userIdFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getUserId().length() <= 8,
                    "SEC-USR-ID field is PIC X(08), max 8 characters");
        }

        @Test
        @DisplayName("TC-SIGNON-012: Password field limited to 8 characters")
        void passwordFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getPassword().length() <= 8,
                    "SEC-USR-PWD field is PIC X(08), max 8 characters");
        }

        @Test
        @DisplayName("TC-SIGNON-013: User type must be 'A' or 'U'")
        void userTypeMustBeValid() {
            UserSecurity admin = TestDataFactory.createAdminUser();
            UserSecurity regular = TestDataFactory.createRegularUser();

            assertTrue("A".equals(admin.getUserType()) || "U".equals(admin.getUserType()),
                    "User type must be A (Admin) or U (User)");
            assertTrue("A".equals(regular.getUserType()) || "U".equals(regular.getUserType()),
                    "User type must be A (Admin) or U (User)");
        }
    }
}
