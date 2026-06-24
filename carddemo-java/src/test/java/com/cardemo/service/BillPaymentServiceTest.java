package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillPaymentService (COBOL COBIL00C equivalent).
 */
@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private BillPaymentService service;

    @BeforeEach
    void setUp() {
        service = new BillPaymentService(accountRepository, cardXrefRepository,
                transactionRepository);
    }

    private Account createAccount(long id, BigDecimal balance) {
        Account account = new Account();
        account.setAccountId(id);
        account.setActiveStatus("Y");
        account.setCurrentBalance(balance);
        account.setCreditLimit(new BigDecimal("10000.00"));
        return account;
    }

    @Nested
    @DisplayName("validateAccount()")
    class ValidateAccountTests {

        @Test
        @DisplayName("Should reject null account ID")
        void shouldRejectNullAccountId() {
            BillPaymentService.PaymentResult result = service.validateAccount(null);

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
            assertEquals("Acct ID can NOT be empty...", result.getMessage());
        }

        @Test
        @DisplayName("Should reject blank account ID")
        void shouldRejectBlankAccountId() {
            BillPaymentService.PaymentResult result = service.validateAccount("   ");

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject non-numeric account ID")
        void shouldRejectNonNumericAccountId() {
            BillPaymentService.PaymentResult result = service.validateAccount("ABC123");

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject account not found")
        void shouldRejectAccountNotFound() {
            when(accountRepository.findByAccountId(99999999999L))
                    .thenReturn(Optional.empty());

            BillPaymentService.PaymentResult result =
                    service.validateAccount("99999999999");

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.ACCOUNT_NOT_FOUND,
                    result.getStatus());
            assertEquals("Account ID NOT found...", result.getMessage());
        }

        @Test
        @DisplayName("Should reject account with zero balance")
        void shouldRejectZeroBalance() {
            Account account = createAccount(12345678901L, BigDecimal.ZERO);
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            BillPaymentService.PaymentResult result =
                    service.validateAccount("12345678901");

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.NOTHING_TO_PAY,
                    result.getStatus());
            assertEquals("You have nothing to pay...", result.getMessage());
        }

        @Test
        @DisplayName("Should reject account with negative balance")
        void shouldRejectNegativeBalance() {
            Account account = createAccount(12345678901L, new BigDecimal("-100.00"));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            BillPaymentService.PaymentResult result =
                    service.validateAccount("12345678901");

            assertNotNull(result);
            assertEquals(BillPaymentService.PaymentResult.Status.NOTHING_TO_PAY,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should return null for valid account with positive balance")
        void shouldReturnNullForValidAccount() {
            Account account = createAccount(12345678901L, new BigDecimal("500.00"));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            BillPaymentService.PaymentResult result =
                    service.validateAccount("12345678901");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("processPayment()")
    class ProcessPaymentTests {

        @Test
        @DisplayName("Should reject null account ID")
        void shouldRejectNullAccountId() {
            BillPaymentService.PaymentResult result = service.processPayment(null);

            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
            assertFalse(result.isSuccessful());
        }

        @Test
        @DisplayName("Should reject blank account ID")
        void shouldRejectBlankAccountId() {
            BillPaymentService.PaymentResult result = service.processPayment("");

            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject non-numeric account ID")
        void shouldRejectNonNumericAccountId() {
            BillPaymentService.PaymentResult result = service.processPayment("ABCDEF");

            assertEquals(BillPaymentService.PaymentResult.Status.INVALID_ACCOUNT_ID,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject account not found")
        void shouldRejectAccountNotFound() {
            when(accountRepository.findByAccountId(99999999999L))
                    .thenReturn(Optional.empty());

            BillPaymentService.PaymentResult result =
                    service.processPayment("99999999999");

            assertEquals(BillPaymentService.PaymentResult.Status.ACCOUNT_NOT_FOUND,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject account with nothing to pay")
        void shouldRejectNothingToPay() {
            Account account = createAccount(12345678901L, BigDecimal.ZERO);
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            BillPaymentService.PaymentResult result =
                    service.processPayment("12345678901");

            assertEquals(BillPaymentService.PaymentResult.Status.NOTHING_TO_PAY,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should reject when xref not found")
        void shouldRejectXrefNotFound() {
            Account account = createAccount(12345678901L, new BigDecimal("500.00"));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.empty());

            BillPaymentService.PaymentResult result =
                    service.processPayment("12345678901");

            assertEquals(BillPaymentService.PaymentResult.Status.XREF_NOT_FOUND,
                    result.getStatus());
        }

        @Test
        @DisplayName("Should process payment successfully")
        void shouldProcessPaymentSuccessfully() {
            Account account = createAccount(12345678901L, new BigDecimal("1500.75"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Transaction lastTx = new Transaction();
            lastTx.setTransactionId("0000000000000099");

            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));
            when(transactionRepository.findLastTransaction())
                    .thenReturn(Optional.of(lastTx));

            BillPaymentService.PaymentResult result =
                    service.processPayment("12345678901");

            assertTrue(result.isSuccessful());
            assertEquals(BillPaymentService.PaymentResult.Status.SUCCESS, result.getStatus());
            assertEquals(new BigDecimal("1500.75"), result.getAmountPaid());

            // Verify transaction was saved
            ArgumentCaptor<Transaction> txCaptor =
                    ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(txCaptor.capture());
            Transaction savedTx = txCaptor.getValue();
            assertEquals("02", savedTx.getTypeCode());
            assertEquals(2, savedTx.getCategoryCode());
            assertEquals("POS TERM", savedTx.getSource());
            assertEquals("BILL PAYMENT - ONLINE", savedTx.getDescription());
            assertEquals(new BigDecimal("1500.75"), savedTx.getAmount());
            assertEquals("1234567890123456", savedTx.getCardNumber());
            assertEquals(999999999L, savedTx.getMerchantId());

            // Verify account balance was zeroed out
            ArgumentCaptor<Account> acctCaptor =
                    ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).update(acctCaptor.capture());
            assertEquals(0, acctCaptor.getValue().getCurrentBalance().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should handle trimmed account ID with spaces")
        void shouldHandleTrimmedAccountId() {
            Account account = createAccount(12345678901L, new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));
            when(transactionRepository.findLastTransaction())
                    .thenReturn(Optional.empty());

            BillPaymentService.PaymentResult result =
                    service.processPayment("  12345678901  ");

            assertTrue(result.isSuccessful());
        }
    }

    @Nested
    @DisplayName("createBillPaymentTransaction()")
    class CreateBillPaymentTransactionTests {

        @Test
        @DisplayName("Should create transaction with correct fields")
        void shouldCreateTransactionWithCorrectFields() {
            Transaction tx = service.createBillPaymentTransaction(
                    100L, new BigDecimal("750.50"), "1234567890123456");

            assertEquals("0000000000000100", tx.getTransactionId());
            assertEquals("02", tx.getTypeCode());
            assertEquals(2, tx.getCategoryCode());
            assertEquals("POS TERM", tx.getSource());
            assertEquals("BILL PAYMENT - ONLINE", tx.getDescription());
            assertEquals(new BigDecimal("750.50"), tx.getAmount());
            assertEquals("1234567890123456", tx.getCardNumber());
            assertEquals(999999999L, tx.getMerchantId());
            assertEquals("BILL PAYMENT", tx.getMerchantName());
            assertEquals("N/A", tx.getMerchantCity());
            assertEquals("N/A", tx.getMerchantZip());
            assertNotNull(tx.getOriginTimestamp());
            assertNotNull(tx.getProcessedTimestamp());
        }

        @Test
        @DisplayName("Should pad transaction ID to 16 digits")
        void shouldPadTransactionIdTo16Digits() {
            Transaction tx = service.createBillPaymentTransaction(
                    1L, BigDecimal.ONE, "1234567890123456");

            assertEquals(16, tx.getTransactionId().length());
            assertEquals("0000000000000001", tx.getTransactionId());
        }
    }

    @Nested
    @DisplayName("getNextTransactionId()")
    class GetNextTransactionIdTests {

        @Test
        @DisplayName("Should return last ID + 1 when transactions exist")
        void shouldReturnIncrementedId() {
            Transaction lastTx = new Transaction();
            lastTx.setTransactionId("0000000000000050");
            when(transactionRepository.findLastTransaction())
                    .thenReturn(Optional.of(lastTx));

            long nextId = service.getNextTransactionId();

            assertEquals(51L, nextId);
        }

        @Test
        @DisplayName("Should return 1 when no transactions exist")
        void shouldReturnOneWhenNoTransactions() {
            when(transactionRepository.findLastTransaction())
                    .thenReturn(Optional.empty());

            long nextId = service.getNextTransactionId();

            assertEquals(1L, nextId);
        }

        @Test
        @DisplayName("Should return 1 when last transaction ID is not numeric")
        void shouldReturnOneForNonNumericId() {
            Transaction lastTx = new Transaction();
            lastTx.setTransactionId("INVALID-ID");
            when(transactionRepository.findLastTransaction())
                    .thenReturn(Optional.of(lastTx));

            long nextId = service.getNextTransactionId();

            assertEquals(1L, nextId);
        }
    }
}
