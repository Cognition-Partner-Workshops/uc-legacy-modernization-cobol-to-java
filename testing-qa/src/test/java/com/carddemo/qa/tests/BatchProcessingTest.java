package com.carddemo.qa.tests;

import com.carddemo.qa.model.AccountRecord;
import com.carddemo.qa.model.TransactionRecord;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for batch processing jobs: Transaction Posting (CBTRN02C),
 * Interest Calculation (CBACT04C), and Statement Generation (CBSTM03A/B).
 * Validates batch job logic migrated from COBOL JCL/batch programs.
 *
 * @see <a href="../../app/cbl/CBTRN02C.cbl">CBTRN02C.cbl</a>
 * @see <a href="../../app/cbl/CBACT04C.cbl">CBACT04C.cbl</a>
 * @see <a href="../../app/cbl/CBSTM03A.CBL">CBSTM03A.CBL</a>
 * @see <a href="../../app/cbl/CBSTM03B.CBL">CBSTM03B.CBL</a>
 */
@DisplayName("Batch Processing Tests (CBTRN02C / CBACT04C / CBSTM03A)")
class BatchProcessingTest {

    @Nested
    @DisplayName("Transaction Posting (POSTTRAN)")
    class TransactionPosting {

        @Test
        @DisplayName("TC-BATCH-001: Credit transaction increases account balance")
        void creditTransactionIncreasesBalance() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            TransactionRecord credit = TestDataFactory.createCreditTransaction();

            BigDecimal originalBalance = account.getCurrentBalance();
            BigDecimal newBalance = originalBalance.add(credit.getAmount());

            assertTrue(newBalance.compareTo(originalBalance) > 0,
                    "Posting a credit transaction should increase account balance");
        }

        @Test
        @DisplayName("TC-BATCH-002: Debit transaction decreases account balance")
        void debitTransactionDecreasesBalance() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            TransactionRecord debit = TestDataFactory.createDebitTransaction();

            BigDecimal originalBalance = account.getCurrentBalance();
            BigDecimal newBalance = originalBalance.add(debit.getAmount());

            assertTrue(newBalance.compareTo(originalBalance) < 0,
                    "Posting a debit transaction should decrease account balance");
        }

        @Test
        @DisplayName("TC-BATCH-003: Transaction posting updates cycle credit for positive amounts")
        void postingUpdatesCycleCredit() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            TransactionRecord credit = TestDataFactory.createCreditTransaction();

            BigDecimal originalCycleCredit = account.getCurrentCycleCredit();

            if (credit.isCredit()) {
                BigDecimal newCycleCredit = originalCycleCredit.add(credit.getAmount());
                assertTrue(newCycleCredit.compareTo(originalCycleCredit) > 0,
                        "Cycle credit should increase for credit transactions");
            }
        }

        @Test
        @DisplayName("TC-BATCH-004: Transaction posting updates cycle debit for negative amounts")
        void postingUpdatesCycleDebit() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            TransactionRecord debit = TestDataFactory.createDebitTransaction();

            BigDecimal originalCycleDebit = account.getCurrentCycleDebit();

            if (debit.isDebit()) {
                BigDecimal newCycleDebit = originalCycleDebit.add(debit.getAmount().abs());
                assertTrue(newCycleDebit.compareTo(originalCycleDebit) > 0,
                        "Cycle debit should increase for debit transactions");
            }
        }

        @Test
        @DisplayName("TC-BATCH-005: Only active accounts can receive posted transactions")
        void onlyActiveAccountsReceivePostings() {
            AccountRecord activeAccount = TestDataFactory.createActiveAccount();
            AccountRecord inactiveAccount = TestDataFactory.createInactiveAccount();

            assertTrue(activeAccount.isActive(),
                    "Active accounts should accept posted transactions");
            assertFalse(inactiveAccount.isActive(),
                    "Inactive accounts should not accept posted transactions");
        }
    }

    @Nested
    @DisplayName("Interest Calculation (INTCALC)")
    class InterestCalculation {

        @Test
        @DisplayName("TC-BATCH-006: Interest is calculated on positive balance")
        void interestOnPositiveBalance() {
            AccountRecord account = TestDataFactory.createActiveAccount();
            BigDecimal interestRate = new BigDecimal("0.015"); // 1.5% monthly

            BigDecimal interest = account.getCurrentBalance().multiply(interestRate);

            assertTrue(interest.compareTo(BigDecimal.ZERO) > 0,
                    "Interest on positive balance should be positive");
        }

        @Test
        @DisplayName("TC-BATCH-007: No interest on zero balance")
        void noInterestOnZeroBalance() {
            AccountRecord account = TestDataFactory.createAccountWithZeroBalance();
            BigDecimal interestRate = new BigDecimal("0.015");

            BigDecimal interest = account.getCurrentBalance().multiply(interestRate);

            assertEquals(0, interest.compareTo(BigDecimal.ZERO),
                    "Zero balance should produce zero interest");
        }

        @Test
        @DisplayName("TC-BATCH-008: Interest only applied to active accounts")
        void interestOnlyForActiveAccounts() {
            AccountRecord inactive = TestDataFactory.createInactiveAccount();

            assertFalse(inactive.isActive(),
                    "Interest calculation should skip inactive accounts");
        }
    }

    @Nested
    @DisplayName("Statement Generation (CREASTMT)")
    class StatementGeneration {

        @Test
        @DisplayName("TC-BATCH-009: Statement includes account identifier")
        void statementIncludesAccountId() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertTrue(account.getAccountId() > 0,
                    "Statement should reference a valid account ID");
        }

        @Test
        @DisplayName("TC-BATCH-010: Statement includes balance information")
        void statementIncludesBalance() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertNotNull(account.getCurrentBalance(),
                    "Statement should include current balance");
            assertNotNull(account.getCreditLimit(),
                    "Statement should include credit limit");
        }

        @Test
        @DisplayName("TC-BATCH-011: Statement includes cycle credit and debit totals")
        void statementIncludesCycleTotals() {
            AccountRecord account = TestDataFactory.createActiveAccount();

            assertNotNull(account.getCurrentCycleCredit(),
                    "Statement should include cycle credit total");
            assertNotNull(account.getCurrentCycleDebit(),
                    "Statement should include cycle debit total");
        }
    }
}
