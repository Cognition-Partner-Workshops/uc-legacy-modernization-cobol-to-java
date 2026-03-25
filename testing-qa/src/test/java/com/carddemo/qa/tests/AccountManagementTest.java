package com.carddemo.qa.tests;

import com.carddemo.qa.model.AccountRecord;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Account View (COACTVWC / CAVW) and Account Update (COACTUPC / CAUP).
 * Validates account record integrity and business rules migrated from COBOL.
 *
 * @see <a href="../../app/cbl/COACTVWC.cbl">COACTVWC.cbl</a>
 * @see <a href="../../app/cbl/COACTUPC.cbl">COACTUPC.cbl</a>
 * @see <a href="../../app/cpy/CVACT01Y.cpy">CVACT01Y.cpy</a>
 */
@DisplayName("Account Management Tests (COACTVWC / COACTUPC)")
class AccountManagementTest {

    @Nested
    @DisplayName("Account View")
    class AccountView {

        @Test
        @DisplayName("TC-ACCT-001: Active account displays correct status")
        void activeAccountDisplaysCorrectStatus() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.isActive(), "Active account should have status 'Y'");
            assertEquals("Y", account.getActiveStatus());
        }

        @Test
        @DisplayName("TC-ACCT-002: Inactive account displays correct status")
        void inactiveAccountDisplaysCorrectStatus() {
            AccountRecord account = TestDataFactory.createInactiveAccount();

            assertFalse(account.isActive(), "Inactive account should have status 'N'");
            assertEquals("N", account.getActiveStatus());
        }

        @Test
        @DisplayName("TC-ACCT-003: Account ID is 11-digit numeric")
        void accountIdIs11DigitNumeric() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            String accountIdStr = String.valueOf(account.getAccountId());

            assertTrue(accountIdStr.length() <= 11,
                    "ACCT-ID is PIC 9(11), max 11 digits");
            assertTrue(account.getAccountId() > 0,
                    "Account ID must be a positive number");
        }

        @Test
        @DisplayName("TC-ACCT-004: Account balance, credit limit, and cycle fields are populated")
        void accountFinancialFieldsPopulated() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertNotNull(account.getCurrentBalance(), "Current balance should not be null");
            assertNotNull(account.getCreditLimit(), "Credit limit should not be null");
            assertNotNull(account.getCashCreditLimit(), "Cash credit limit should not be null");
            assertNotNull(account.getCurrentCycleCredit(), "Current cycle credit should not be null");
            assertNotNull(account.getCurrentCycleDebit(), "Current cycle debit should not be null");
        }
    }

    @Nested
    @DisplayName("Account Update Validations")
    class AccountUpdate {

        @Test
        @DisplayName("TC-ACCT-005: Account status must be 'Y' or 'N'")
        void accountStatusValidValues() {
            AccountRecord active = TestDataFactory.createActiveAccount();
            AccountRecord inactive = TestDataFactory.createInactiveAccount();

            assertTrue("Y".equals(active.getActiveStatus()) || "N".equals(active.getActiveStatus()),
                    "ACCT-ACTIVE-STATUS must be 'Y' or 'N'");
            assertTrue("Y".equals(inactive.getActiveStatus()) || "N".equals(inactive.getActiveStatus()),
                    "ACCT-ACTIVE-STATUS must be 'Y' or 'N'");
        }

        @Test
        @DisplayName("TC-ACCT-006: Credit limit must be non-negative")
        void creditLimitNonNegative() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.getCreditLimit().compareTo(BigDecimal.ZERO) >= 0,
                    "Credit limit cannot be negative");
        }

        @Test
        @DisplayName("TC-ACCT-007: Cash credit limit must not exceed credit limit")
        void cashCreditLimitDoesNotExceedCreditLimit() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.getCashCreditLimit().compareTo(account.getCreditLimit()) <= 0,
                    "Cash credit limit should not exceed the overall credit limit");
        }

        @Test
        @DisplayName("TC-ACCT-008: Open date, expiration date, and reissue date are present")
        void dateFieldsPresent() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertNotNull(account.getOpenDate(), "Open date must be present");
            assertNotNull(account.getExpirationDate(), "Expiration date must be present");
            assertNotNull(account.getReissueDate(), "Reissue date must be present");
            assertFalse(account.getOpenDate().isBlank(), "Open date must not be blank");
            assertFalse(account.getExpirationDate().isBlank(), "Expiration date must not be blank");
        }

        @Test
        @DisplayName("TC-ACCT-009: Address ZIP code field limited to 10 characters")
        void zipCodeFieldLength() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.getAddressZip().length() <= 10,
                    "ACCT-ADDR-ZIP is PIC X(10), max 10 characters");
        }

        @Test
        @DisplayName("TC-ACCT-010: Account with zero balance has correct cycle totals")
        void zeroBalanceAccountCycleTotals() {
            AccountRecord account = TestDataFactory.createAccountWithZeroBalance();

            assertEquals(0, account.getCurrentBalance().compareTo(BigDecimal.ZERO),
                    "Zero-balance account should have $0.00 balance");
            assertEquals(0, account.getCurrentCycleCredit().compareTo(BigDecimal.ZERO),
                    "Zero-balance account cycle credit should be $0.00");
            assertEquals(0, account.getCurrentCycleDebit().compareTo(BigDecimal.ZERO),
                    "Zero-balance account cycle debit should be $0.00");
        }
    }

    @Nested
    @DisplayName("Account Group ID")
    class AccountGroupId {

        @Test
        @DisplayName("TC-ACCT-011: Group ID field limited to 10 characters")
        void groupIdFieldLength() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.getGroupId().length() <= 10,
                    "ACCT-GROUP-ID is PIC X(10), max 10 characters");
        }
    }
}
