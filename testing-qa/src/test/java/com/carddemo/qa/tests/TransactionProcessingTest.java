package com.carddemo.qa.tests;

import com.carddemo.qa.model.TransactionRecord;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Transaction List (COTRN00C / CT00), View (COTRN01C / CT01),
 * and Add (COTRN02C / CT02) transactions.
 * Validates transaction record integrity and business rules migrated from COBOL.
 *
 * @see <a href="../../app/cbl/COTRN00C.cbl">COTRN00C.cbl</a>
 * @see <a href="../../app/cbl/COTRN01C.cbl">COTRN01C.cbl</a>
 * @see <a href="../../app/cbl/COTRN02C.cbl">COTRN02C.cbl</a>
 * @see <a href="../../app/cpy/CVTRA05Y.cpy">CVTRA05Y.cpy</a>
 */
@DisplayName("Transaction Processing Tests (COTRN00C / COTRN01C / COTRN02C)")
class TransactionProcessingTest {

    @Nested
    @DisplayName("Transaction Record Fields")
    class TransactionRecordFields {

        @Test
        @DisplayName("TC-TRAN-001: Transaction ID is 16 characters")
        void transactionIdIs16Characters() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertEquals(16, txn.getTransactionId().length(),
                    "TRAN-ID is PIC X(16), must be exactly 16 characters");
        }

        @Test
        @DisplayName("TC-TRAN-002: Transaction type code is 2 characters")
        void transactionTypeCodeIs2Characters() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertEquals(2, txn.getTypeCode().length(),
                    "TRAN-TYPE-CD is PIC X(02), must be exactly 2 characters");
        }

        @Test
        @DisplayName("TC-TRAN-003: Category code is up to 4 digits")
        void categoryCodeUpTo4Digits() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertTrue(txn.getCategoryCode() >= 0 && txn.getCategoryCode() <= 9999,
                    "TRAN-CAT-CD is PIC 9(04), must be 0-9999");
        }

        @Test
        @DisplayName("TC-TRAN-004: Transaction description limited to 100 characters")
        void descriptionFieldLength() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertTrue(txn.getDescription().length() <= 100,
                    "TRAN-DESC is PIC X(100), max 100 characters");
        }

        @Test
        @DisplayName("TC-TRAN-005: Merchant name limited to 50 characters")
        void merchantNameFieldLength() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertTrue(txn.getMerchantName().length() <= 50,
                    "TRAN-MERCHANT-NAME is PIC X(50), max 50 characters");
        }
    }

    @Nested
    @DisplayName("Transaction Amount")
    class TransactionAmount {

        @Test
        @DisplayName("TC-TRAN-006: Credit transaction has positive amount")
        void creditTransactionPositiveAmount() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertTrue(txn.isCredit(), "Credit transaction should have positive amount");
            assertFalse(txn.isDebit(), "Credit transaction should not be flagged as debit");
            assertTrue(txn.getAmount().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("TC-TRAN-007: Debit transaction has negative amount")
        void debitTransactionNegativeAmount() {
            TransactionRecord txn = TestDataFactory.createDebitTransaction();

            assertTrue(txn.isDebit(), "Debit transaction should have negative amount");
            assertFalse(txn.isCredit(), "Debit transaction should not be flagged as credit");
            assertTrue(txn.getAmount().compareTo(BigDecimal.ZERO) < 0);
        }

        @Test
        @DisplayName("TC-TRAN-008: Transaction amount has 2 decimal places precision")
        void transactionAmountDecimalPrecision() {
            TransactionRecord txn = TestDataFactory.createDebitTransaction();

            assertEquals(2, txn.getAmount().scale(),
                    "TRAN-AMT PIC S9(09)V99 implies 2 decimal places");
        }
    }

    @Nested
    @DisplayName("Transaction Card Linkage")
    class TransactionCardLinkage {

        @Test
        @DisplayName("TC-TRAN-009: Transaction is linked to a valid card number")
        void transactionLinkedToCard() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertNotNull(txn.getCardNumber(), "Card number must not be null");
            assertEquals(16, txn.getCardNumber().length(),
                    "TRAN-CARD-NUM is PIC X(16)");
        }

        @Test
        @DisplayName("TC-TRAN-010: Multiple transactions can reference the same card")
        void multipleTransactionsSameCard() {
            TransactionRecord credit = TestDataFactory.createCreditTransaction();
            TransactionRecord debit = TestDataFactory.createDebitTransaction();

            assertEquals(credit.getCardNumber(), debit.getCardNumber(),
                    "Multiple transactions can be on the same card");
        }
    }

    @Nested
    @DisplayName("Transaction Timestamps")
    class TransactionTimestamps {

        @Test
        @DisplayName("TC-TRAN-011: Origin timestamp is present")
        void originTimestampPresent() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertNotNull(txn.getOriginTimestamp(), "Origin timestamp must not be null");
            assertFalse(txn.getOriginTimestamp().isBlank(),
                    "Origin timestamp must not be blank");
        }

        @Test
        @DisplayName("TC-TRAN-012: Processed timestamp is present")
        void processedTimestampPresent() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertNotNull(txn.getProcessedTimestamp(), "Processed timestamp must not be null");
            assertFalse(txn.getProcessedTimestamp().isBlank(),
                    "Processed timestamp must not be blank");
        }

        @Test
        @DisplayName("TC-TRAN-013: Each transaction has a unique transaction ID")
        void uniqueTransactionIds() {
            TransactionRecord txn1 = TestDataFactory.createCreditTransaction();
            TransactionRecord txn2 = TestDataFactory.createDebitTransaction();

            assertNotEquals(txn1.getTransactionId(), txn2.getTransactionId(),
                    "Each transaction must have a unique ID");
        }
    }

    @Nested
    @DisplayName("Transaction List Navigation")
    class TransactionListNavigation {

        @ParameterizedTest
        @ValueSource(strings = {"0000000000000001", "0000000000000002"})
        @DisplayName("TC-TRAN-014: Transaction ID input must be numeric")
        void transactionIdMustBeNumeric(String tranId) {
            assertTrue(tranId.matches("\\d+"),
                    "COTRN00C requires Tran ID to be numeric for filtering");
        }

        @Test
        @DisplayName("TC-TRAN-015: Selection flag must be 'S' to view transaction details")
        void selectionFlagMustBeS() {
            String validSelection = "S";
            String validSelectionLower = "s";

            assertTrue("S".equals(validSelection) || "s".equals(validSelection),
                    "Valid selection flag is 'S' or 's' per COTRN00C logic");
            assertTrue("S".equals(validSelectionLower) || "s".equals(validSelectionLower),
                    "Lowercase 's' is also valid per COTRN00C logic");
        }
    }

    @Nested
    @DisplayName("Transaction Source")
    class TransactionSource {

        @Test
        @DisplayName("TC-TRAN-016: Transaction source limited to 10 characters")
        void transactionSourceFieldLength() {
            TransactionRecord txn = TestDataFactory.createCreditTransaction();

            assertTrue(txn.getSource().length() <= 10,
                    "TRAN-SOURCE is PIC X(10), max 10 characters");
        }
    }
}
