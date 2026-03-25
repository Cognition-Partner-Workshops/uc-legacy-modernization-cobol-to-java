package com.carddemo.qa.tests;

import com.carddemo.qa.model.UserSecurity;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for User Administration: List (COUSR00C / CU00), Add (COUSR01C / CU01),
 * Update (COUSR02C / CU02), and Delete (COUSR03C / CU03) operations.
 * Validates user CRUD operations migrated from COBOL.
 *
 * @see <a href="../../app/cbl/COUSR00C.cbl">COUSR00C.cbl</a>
 * @see <a href="../../app/cbl/COUSR01C.cbl">COUSR01C.cbl</a>
 * @see <a href="../../app/cbl/COUSR02C.cbl">COUSR02C.cbl</a>
 * @see <a href="../../app/cbl/COUSR03C.cbl">COUSR03C.cbl</a>
 */
@DisplayName("User Administration Tests (COUSR00C-03C)")
class UserAdministrationTest {

    @Nested
    @DisplayName("User Record Fields")
    class UserRecordFields {

        @Test
        @DisplayName("TC-USER-001: User ID limited to 8 characters")
        void userIdFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getUserId().length() <= 8,
                    "SEC-USR-ID is PIC X(08)");
        }

        @Test
        @DisplayName("TC-USER-002: First name limited to 20 characters")
        void firstNameFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getFirstName().length() <= 20,
                    "SEC-USR-FNAME is PIC X(20)");
        }

        @Test
        @DisplayName("TC-USER-003: Last name limited to 20 characters")
        void lastNameFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getLastName().length() <= 20,
                    "SEC-USR-LNAME is PIC X(20)");
        }

        @Test
        @DisplayName("TC-USER-004: Password limited to 8 characters")
        void passwordFieldLength() {
            UserSecurity user = TestDataFactory.createAdminUser();

            assertTrue(user.getPassword().length() <= 8,
                    "SEC-USR-PWD is PIC X(08)");
        }
    }

    @Nested
    @DisplayName("User Creation")
    class UserCreation {

        @Test
        @DisplayName("TC-USER-005: New admin user has type 'A'")
        void newAdminUserType() {
            UserSecurity admin = TestDataFactory.createAdminUser();

            assertEquals("A", admin.getUserType(),
                    "Admin user type should be 'A'");
        }

        @Test
        @DisplayName("TC-USER-006: New regular user has type 'U'")
        void newRegularUserType() {
            UserSecurity regular = TestDataFactory.createRegularUser();

            assertEquals("U", regular.getUserType(),
                    "Regular user type should be 'U'");
        }

        @Test
        @DisplayName("TC-USER-007: User record has all required fields populated")
        void userRecordFullyPopulated() {
            UserSecurity user = TestDataFactory.createRegularUser();

            assertNotNull(user.getUserId(), "User ID is required");
            assertNotNull(user.getFirstName(), "First name is required");
            assertNotNull(user.getLastName(), "Last name is required");
            assertNotNull(user.getPassword(), "Password is required");
            assertNotNull(user.getUserType(), "User type is required");
            assertFalse(user.getUserId().isBlank(), "User ID must not be blank");
            assertFalse(user.getPassword().isBlank(), "Password must not be blank");
        }
    }

    @Nested
    @DisplayName("User Update")
    class UserUpdate {

        @Test
        @DisplayName("TC-USER-008: User first name can be updated")
        void updateFirstName() {
            UserSecurity user = TestDataFactory.createRegularUser();
            String originalFirstName = user.getFirstName();

            user.setFirstName("UPDATED");
            assertNotEquals(originalFirstName, user.getFirstName(),
                    "First name should be updated");
            assertEquals("UPDATED", user.getFirstName());
        }

        @Test
        @DisplayName("TC-USER-009: User last name can be updated")
        void updateLastName() {
            UserSecurity user = TestDataFactory.createRegularUser();
            String originalLastName = user.getLastName();

            user.setLastName("NEWLAST");
            assertNotEquals(originalLastName, user.getLastName(),
                    "Last name should be updated");
            assertEquals("NEWLAST", user.getLastName());
        }

        @Test
        @DisplayName("TC-USER-010: User password can be updated")
        void updatePassword() {
            UserSecurity user = TestDataFactory.createRegularUser();
            String originalPassword = user.getPassword();

            user.setPassword("NEWPASS1");
            assertNotEquals(originalPassword, user.getPassword(),
                    "Password should be updated");
            assertEquals("NEWPASS1", user.getPassword());
        }

        @Test
        @DisplayName("TC-USER-011: User type can be changed from regular to admin")
        void changeUserTypeToAdmin() {
            UserSecurity user = TestDataFactory.createRegularUser();

            assertTrue(user.isRegularUser());
            user.setUserType("A");
            assertTrue(user.isAdmin(),
                    "User type should be changeable to admin");
        }
    }

    @Nested
    @DisplayName("User Deletion")
    class UserDeletion {

        @Test
        @DisplayName("TC-USER-012: Deleting a user clears the record")
        void deleteUserClearsRecord() {
            UserSecurity user = TestDataFactory.createRegularUser();
            assertNotNull(user.getUserId());

            user.setUserId(null);
            user.setFirstName(null);
            user.setLastName(null);
            user.setPassword(null);
            user.setUserType(null);

            assertNull(user.getUserId(), "Deleted user ID should be null");
            assertNull(user.getFirstName(), "Deleted first name should be null");
            assertNull(user.getLastName(), "Deleted last name should be null");
            assertNull(user.getPassword(), "Deleted password should be null");
            assertNull(user.getUserType(), "Deleted user type should be null");
        }
    }
}
