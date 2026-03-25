package com.cardemo.service;

import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import com.cardemo.util.DateValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionAddService (COBOL COTRN02C equivalent).
 */
@ExtendWith(MockitoExtension.class)
class TransactionAddServiceTest {

    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private DateValidationUtil dateValidationUtil;
    private TransactionAddService service;

    @BeforeEach
    void setUp() {
        dateValidationUtil = new DateValidationUtil();
        service = new TransactionAddService(cardXrefRepository,
                transactionRepository, dateValidationUtil);
    }

    @Nested
    @DisplayName("validateKeyFields()")
    class ValidateKeyFieldsTests {

        @Test
        @DisplayName("Should reject when both account ID and card number are null")
        void shouldRejectBothNull() {
            List<String> errors = service.validateKeyFields(null, null);

            assertEquals(1, errors.size());
            assertEquals("Account or Card Number must be entered...", errors.get(0));
        }

        @Test
        @DisplayName("Should reject when both account ID and card number are blank")
        void shouldRejectBothBlank() {
            List<String> errors = service.validateKeyFields("", "  ");

            assertEquals(1, errors.size());
            assertEquals("Account or Card Number must be entered...", errors.get(0));
        }

        @Test
        @DisplayName("Should reject non-numeric account ID")
        void shouldRejectNonNumericAccountId() {
            List<String> errors = service.validateKeyFields("ABC123", null);

            assertEquals(1, errors.size());
            assertEquals("Account ID must be Numeric...", errors.get(0));
        }

        @Test
        @DisplayName("Should reject non-numeric card number")
        void shouldRejectNonNumericCardNumber() {
            List<String> errors = service.validateKeyFields(null, "12AB5678");

            assertEquals(1, errors.size());
            assertEquals("Card Number must be Numeric...", errors.get(0));
        }

        @Test
        @DisplayName("Should accept valid numeric account ID")
        void shouldAcceptValidAccountId() {
            List<String> errors = service.validateKeyFields("12345678901", null);

            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should accept valid numeric card number")
        void shouldAcceptValidCardNumber() {
            List<String> errors = service.validateKeyFields(null, "1234567890123456");

            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should accept both valid account ID and card number")
        void shouldAcceptBothValid() {
            List<String> errors = service.validateKeyFields("12345678901",
                    "1234567890123456");

            assertTrue(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("validateDataFields()")
    class ValidateDataFieldsTests {

        @Test
        @DisplayName("Should reject all empty fields")
        void shouldRejectAllEmptyFields() {
            List<String> errors = service.validateDataFields(
                    "", "", "", "", "", "", "", "", "", "", "");

            assertFalse(errors.isEmpty());
            assertTrue(errors.stream().anyMatch(e -> e.contains("Type CD")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Category CD")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Source")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Description")));
            assertTrue(errors.stream().anyMatch(e -> e.contains("Amount")));
        }

        @Test
        @DisplayName("Should reject null required fields")
        void shouldRejectNullFields() {
            List<String> errors = service.validateDataFields(
                    null, null, null, null, null, null, null, null, null, null, null);

            assertFalse(errors.isEmpty());
            assertTrue(errors.size() >= 5);
        }

        @Test
        @DisplayName("Should reject non-numeric type code")
        void shouldRejectNonNumericTypeCode() {
            List<String> errors = service.validateDataFields(
                    "AB", "0001", "POS", "Desc", "+100.00",
                    "2025-01-15", "2025-01-15", "123456789",
                    "Merchant", "City", "12345");

            assertTrue(errors.stream().anyMatch(e -> e.contains("Type CD must be Numeric")));
        }

        @Test
        @DisplayName("Should reject non-numeric category code")
        void shouldRejectNonNumericCategoryCode() {
            List<String> errors = service.validateDataFields(
                    "01", "ABCD", "POS", "Desc", "+100.00",
                    "2025-01-15", "2025-01-15", "123456789",
                    "Merchant", "City", "12345");

            assertTrue(errors.stream().anyMatch(e ->
                    e.contains("Category CD must be Numeric")));
        }

        @Test
        @DisplayName("Should reject invalid amount format")
        void shouldRejectInvalidAmountFormat() {
            List<String> errors = service.validateDataFields(
                    "01", "0001", "POS", "Desc", "100.00",  // missing sign
                    "2025-01-15", "2025-01-15", "123456789",
                    "Merchant", "City", "12345");

            assertTrue(errors.stream().anyMatch(e -> e.contains("Amount should be")));
        }

        @Test
        @DisplayName("Should reject invalid date format")
        void shouldRejectInvalidDateFormat() {
            List<String> errors = service.validateDataFields(
                    "01", "0001", "POS", "Desc", "+100.00",
                    "01/15/2025", "2025-01-15", "123456789",
                    "Merchant", "City", "12345");

            assertTrue(errors.stream().anyMatch(e ->
                    e.contains("Orig Date should be")));
        }

        @Test
        @DisplayName("Should accept all valid data fields")
        void shouldAcceptAllValidFields() {
            List<String> errors = service.validateDataFields(
                    "01", "0001", "POS TERM", "Test purchase", "+100.00",
                    "2025-01-15", "2025-01-15", "123456789",
                    "Test Merchant", "Test City", "12345");

            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should accept negative amount")
        void shouldAcceptNegativeAmount() {
            List<String> errors = service.validateDataFields(
                    "01", "0001", "POS TERM", "Refund", "-50.00",
                    "2025-01-15", "2025-01-15", "123456789",
                    "Test Merchant", "Test City", "12345");

            assertTrue(errors.isEmpty());
        }
    }

    @Nested
    @DisplayName("isValidAmount()")
    class IsValidAmountTests {

        @Test
        @DisplayName("Should accept positive amount with sign")
        void shouldAcceptPositiveAmount() {
            assertTrue(service.isValidAmount("+100.00"));
        }

        @Test
        @DisplayName("Should accept negative amount with sign")
        void shouldAcceptNegativeAmount() {
            assertTrue(service.isValidAmount("-50.00"));
        }

        @Test
        @DisplayName("Should reject amount without sign")
        void shouldRejectNoSign() {
            assertFalse(service.isValidAmount("100.00"));
        }

        @Test
        @DisplayName("Should reject amount without decimal")
        void shouldRejectNoDecimal() {
            assertFalse(service.isValidAmount("+100"));
        }

        @Test
        @DisplayName("Should reject amount with one decimal digit")
        void shouldRejectOneDecimalDigit() {
            assertFalse(service.isValidAmount("+100.0"));
        }

        @Test
        @DisplayName("Should reject null amount")
        void shouldRejectNullAmount() {
            assertFalse(service.isValidAmount(null));
        }

        @Test
        @DisplayName("Should reject short amount")
        void shouldRejectShortAmount() {
            assertFalse(service.isValidAmount("+.0"));
        }

        @Test
        @DisplayName("Should accept large amount")
        void shouldAcceptLargeAmount() {
            assertTrue(service.isValidAmount("+99999999.99"));
        }

        @Test
        @DisplayName("Should reject letters in integer part")
        void shouldRejectLettersInIntegerPart() {
            assertFalse(service.isValidAmount("+10A.00"));
        }

        @Test
        @DisplayName("Should reject letters in decimal part")
        void shouldRejectLettersInDecimalPart() {
            assertFalse(service.isValidAmount("+100.AB"));
        }
    }

    @Nested
    @DisplayName("isValidDateFormat()")
    class IsValidDateFormatTests {

        @Test
        @DisplayName("Should accept valid YYYY-MM-DD date")
        void shouldAcceptValidDate() {
            assertTrue(service.isValidDateFormat("2025-01-15"));
        }

        @Test
        @DisplayName("Should reject date with wrong separator")
        void shouldRejectWrongSeparator() {
            assertFalse(service.isValidDateFormat("2025/01/15"));
        }

        @Test
        @DisplayName("Should reject date with wrong length")
        void shouldRejectWrongLength() {
            assertFalse(service.isValidDateFormat("2025-1-15"));
        }

        @Test
        @DisplayName("Should reject null date")
        void shouldRejectNullDate() {
            assertFalse(service.isValidDateFormat(null));
        }

        @Test
        @DisplayName("Should reject non-numeric components")
        void shouldRejectNonNumericDate() {
            assertFalse(service.isValidDateFormat("YYYY-MM-DD"));
        }

        @Test
        @DisplayName("Should reject invalid month")
        void shouldRejectInvalidMonth() {
            assertFalse(service.isValidDateFormat("2025-13-01"));
        }

        @Test
        @DisplayName("Should reject invalid day")
        void shouldRejectInvalidDay() {
            assertFalse(service.isValidDateFormat("2025-02-30"));
        }
    }

    @Nested
    @DisplayName("lookupCardNumber()")
    class LookupCardNumberTests {

        @Test
        @DisplayName("Should return card number when xref exists")
        void shouldReturnCardNumber() {
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));

            Optional<String> result = service.lookupCardNumber("12345678901");

            assertTrue(result.isPresent());
            assertEquals("1234567890123456", result.get());
        }

        @Test
        @DisplayName("Should return empty when xref not found")
        void shouldReturnEmptyWhenNotFound() {
            when(cardXrefRepository.findByAccountId(99999999999L))
                    .thenReturn(Optional.empty());

            Optional<String> result = service.lookupCardNumber("99999999999");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty for null account ID")
        void shouldReturnEmptyForNull() {
            Optional<String> result = service.lookupCardNumber(null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty for blank account ID")
        void shouldReturnEmptyForBlank() {
            Optional<String> result = service.lookupCardNumber("  ");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty for non-numeric account ID")
        void shouldReturnEmptyForNonNumeric() {
            Optional<String> result = service.lookupCardNumber("ABC");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("lookupAccountId()")
    class LookupAccountIdTests {

        @Test
        @DisplayName("Should return account ID when xref exists")
        void shouldReturnAccountId() {
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));

            Optional<Long> result = service.lookupAccountId("1234567890123456");

            assertTrue(result.isPresent());
            assertEquals(12345678901L, result.get());
        }

        @Test
        @DisplayName("Should return empty when xref not found")
        void shouldReturnEmptyWhenNotFound() {
            when(cardXrefRepository.findByCardNumber("9999999999999999"))
                    .thenReturn(Optional.empty());

            Optional<Long> result = service.lookupAccountId("9999999999999999");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty for null card number")
        void shouldReturnEmptyForNull() {
            Optional<Long> result = service.lookupAccountId(null);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty for blank card number")
        void shouldReturnEmptyForBlank() {
            Optional<Long> result = service.lookupAccountId("  ");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addTransaction()")
    class AddTransactionTests {

        @Test
        @DisplayName("Should save transaction when confirmed with Y")
        void shouldSaveWhenConfirmedY() {
            Transaction tx = new Transaction();
            tx.setTransactionId("0000000000000001");

            String error = service.addTransaction(tx, "Y");

            assertNull(error);
            verify(transactionRepository).save(tx);
        }

        @Test
        @DisplayName("Should save transaction when confirmed with lowercase y")
        void shouldSaveWhenConfirmedLowercaseY() {
            Transaction tx = new Transaction();
            tx.setTransactionId("0000000000000001");

            String error = service.addTransaction(tx, "y");

            assertNull(error);
            verify(transactionRepository).save(tx);
        }

        @Test
        @DisplayName("Should not save when confirmed with N")
        void shouldNotSaveWhenConfirmedN() {
            Transaction tx = new Transaction();

            String error = service.addTransaction(tx, "N");

            assertNotNull(error);
            assertEquals("Confirm to add this transaction...", error);
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject invalid confirmation value")
        void shouldRejectInvalidConfirmation() {
            Transaction tx = new Transaction();

            String error = service.addTransaction(tx, "X");

            assertNotNull(error);
            assertEquals("Invalid value. Valid values are (Y/N)...", error);
            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prompt when confirmation is null")
        void shouldPromptWhenNull() {
            Transaction tx = new Transaction();

            String error = service.addTransaction(tx, null);

            assertNotNull(error);
            assertEquals("Confirm to add this transaction...", error);
        }

        @Test
        @DisplayName("Should prompt when confirmation is blank")
        void shouldPromptWhenBlank() {
            Transaction tx = new Transaction();

            String error = service.addTransaction(tx, "  ");

            assertNotNull(error);
            assertEquals("Confirm to add this transaction...", error);
        }

        @Test
        @DisplayName("Should trim confirmation before checking")
        void shouldTrimConfirmation() {
            Transaction tx = new Transaction();
            tx.setTransactionId("0000000000000001");

            String error = service.addTransaction(tx, "  Y  ");

            assertNull(error);
            verify(transactionRepository).save(tx);
        }
    }
}
