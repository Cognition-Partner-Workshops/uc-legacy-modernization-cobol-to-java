package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionPostingService (COBOL CBTRN02C equivalent).
 */
@ExtendWith(MockitoExtension.class)
class TransactionPostingServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionCategoryBalanceRepository categoryBalanceRepository;

    private TransactionPostingService service;

    @BeforeEach
    void setUp() {
        service = new TransactionPostingService(
                transactionRepository, cardXrefRepository,
                accountRepository, categoryBalanceRepository);
    }

    private Transaction createDailyTransaction(String cardNum, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setTransactionId("0000000000000001");
        tx.setTypeCode("01");
        tx.setCategoryCode(1);
        tx.setSource("POS TERM");
        tx.setDescription("Test purchase");
        tx.setAmount(amount);
        tx.setMerchantId(123456789L);
        tx.setMerchantName("Test Merchant");
        tx.setMerchantCity("Test City");
        tx.setMerchantZip("12345");
        tx.setCardNumber(cardNum);
        tx.setOriginTimestamp("2025-01-15-10.30.00.000000");
        return tx;
    }

    private Account createAccount(long id, BigDecimal balance, BigDecimal creditLimit,
                                   BigDecimal cycCredit, BigDecimal cycDebit,
                                   String expirationDate) {
        Account acct = new Account();
        acct.setAccountId(id);
        acct.setActiveStatus("Y");
        acct.setCurrentBalance(balance);
        acct.setCreditLimit(creditLimit);
        acct.setCurrentCycleCredit(cycCredit);
        acct.setCurrentCycleDebit(cycDebit);
        acct.setExpirationDate(expirationDate);
        acct.setGroupId("GROUP1");
        return acct;
    }

    @Nested
    @DisplayName("validateTransaction()")
    class ValidateTransactionTests {

        @Test
        @DisplayName("Should reject transaction with invalid card number")
        void shouldRejectInvalidCardNumber() {
            Transaction tx = createDailyTransaction("9999999999999999",
                    new BigDecimal("100.00"));
            when(cardXrefRepository.findByCardNumber("9999999999999999"))
                    .thenReturn(Optional.empty());

            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertFalse(result.isValid());
            assertEquals(100, result.getFailReasonCode());
            assertEquals("INVALID CARD NUMBER FOUND", result.getFailReasonDescription());
        }

        @Test
        @DisplayName("Should reject transaction when account not found")
        void shouldRejectAccountNotFound() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.empty());

            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertFalse(result.isValid());
            assertEquals(101, result.getFailReasonCode());
            assertEquals("ACCOUNT RECORD NOT FOUND", result.getFailReasonDescription());
        }

        @Test
        @DisplayName("Should reject overlimit transaction")
        void shouldRejectOverlimitTransaction() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("5000.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("5000.00"),       // credit limit
                    new BigDecimal("2000.00"),        // cycle credit
                    new BigDecimal("500.00"),          // cycle debit
                    "2030-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            // tempBalance = 2000 - 500 + 5000 = 6500 > 5000 (credit limit)
            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertFalse(result.isValid());
            assertEquals(102, result.getFailReasonCode());
            assertEquals("OVERLIMIT TRANSACTION", result.getFailReasonDescription());
        }

        @Test
        @DisplayName("Should reject expired account transaction")
        void shouldRejectExpiredAccountTransaction() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            tx.setOriginTimestamp("2025-06-15-10.30.00.000000");
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("10000.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "2025-01-01");  // expired before transaction date

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertFalse(result.isValid());
            assertEquals(103, result.getFailReasonCode());
            assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION",
                    result.getFailReasonDescription());
        }

        @Test
        @DisplayName("Should validate a valid transaction successfully")
        void shouldValidateValidTransaction() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("500.00"),
                    new BigDecimal("10000.00"),
                    new BigDecimal("200.00"),
                    new BigDecimal("100.00"),
                    "2030-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertTrue(result.isValid());
            assertEquals(0, result.getFailReasonCode());
        }

        @Test
        @DisplayName("Should accept transaction at exactly the credit limit")
        void shouldAcceptTransactionAtCreditLimit() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("5000.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("500.00"),
                    new BigDecimal("5000.00"),    // credit limit exactly
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "2030-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            // tempBalance = 0 - 0 + 5000 = 5000 == 5000 (credit limit)
            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should accept transaction on expiration date")
        void shouldAcceptTransactionOnExpirationDate() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            tx.setOriginTimestamp("2025-12-31-10.30.00.000000");
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("500.00"),
                    new BigDecimal("10000.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "2025-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));

            TransactionPostingService.ValidationResult result =
                    service.validateTransaction(tx);

            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("postTransaction()")
    class PostTransactionTests {

        @Test
        @DisplayName("Should post transaction and update all records")
        void shouldPostTransactionSuccessfully() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("500.00"),
                    new BigDecimal("10000.00"),
                    new BigDecimal("200.00"),
                    BigDecimal.ZERO,
                    "2030-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(categoryBalanceRepository.findByKey(12345678901L, "01", 1))
                    .thenReturn(Optional.empty());

            service.postTransaction(tx);

            // Verify transaction was saved
            verify(transactionRepository).save(any(Transaction.class));

            // Verify new category balance was created
            verify(categoryBalanceRepository).save(any(TransactionCategoryBalance.class));

            // Verify account was updated
            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).update(accountCaptor.capture());
            Account updatedAccount = accountCaptor.getValue();
            assertEquals(new BigDecimal("600.00"), updatedAccount.getCurrentBalance());
            assertEquals(new BigDecimal("300.00"), updatedAccount.getCurrentCycleCredit());
        }

        @Test
        @DisplayName("Should throw when card xref not found during posting")
        void shouldThrowWhenXrefNotFound() {
            Transaction tx = createDailyTransaction("9999999999999999",
                    new BigDecimal("100.00"));
            when(cardXrefRepository.findByCardNumber("9999999999999999"))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> service.postTransaction(tx));
        }

        @Test
        @DisplayName("Should throw when account not found during posting")
        void shouldThrowWhenAccountNotFound() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> service.postTransaction(tx));
        }
    }

    @Nested
    @DisplayName("updateAccountRecord()")
    class UpdateAccountRecordTests {

        @Test
        @DisplayName("Should add positive amount to cycle credit")
        void shouldAddPositiveAmountToCycleCredit() {
            Account account = createAccount(12345678901L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("10000.00"),
                    new BigDecimal("200.00"),
                    BigDecimal.ZERO,
                    "2030-12-31");

            service.updateAccountRecord(account, new BigDecimal("150.00"));

            assertEquals(new BigDecimal("1150.00"), account.getCurrentBalance());
            assertEquals(new BigDecimal("350.00"), account.getCurrentCycleCredit());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleDebit());
            verify(accountRepository).update(account);
        }

        @Test
        @DisplayName("Should add negative amount to cycle debit")
        void shouldAddNegativeAmountToCycleDebit() {
            Account account = createAccount(12345678901L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("10000.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("-50.00"),
                    "2030-12-31");

            service.updateAccountRecord(account, new BigDecimal("-75.00"));

            assertEquals(new BigDecimal("925.00"), account.getCurrentBalance());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
            assertEquals(new BigDecimal("-125.00"), account.getCurrentCycleDebit());
            verify(accountRepository).update(account);
        }

        @Test
        @DisplayName("Should add zero amount to cycle credit")
        void shouldAddZeroAmountToCycleCredit() {
            Account account = createAccount(12345678901L,
                    new BigDecimal("1000.00"),
                    new BigDecimal("10000.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "2030-12-31");

            service.updateAccountRecord(account, BigDecimal.ZERO);

            assertEquals(new BigDecimal("1000.00"), account.getCurrentBalance());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
        }
    }

    @Nested
    @DisplayName("updateCategoryBalance()")
    class UpdateCategoryBalanceTests {

        @Test
        @DisplayName("Should create new category balance when not existing")
        void shouldCreateNewCategoryBalance() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            when(categoryBalanceRepository.findByKey(12345678901L, "01", 1))
                    .thenReturn(Optional.empty());

            service.updateCategoryBalance(12345678901L, tx);

            ArgumentCaptor<TransactionCategoryBalance> captor =
                    ArgumentCaptor.forClass(TransactionCategoryBalance.class);
            verify(categoryBalanceRepository).save(captor.capture());
            TransactionCategoryBalance saved = captor.getValue();
            assertEquals(12345678901L, saved.getAccountId());
            assertEquals("01", saved.getTypeCode());
            assertEquals(1, saved.getCategoryCode());
            assertEquals(new BigDecimal("100.00"), saved.getBalance());
        }

        @Test
        @DisplayName("Should update existing category balance")
        void shouldUpdateExistingCategoryBalance() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            TransactionCategoryBalance existing = new TransactionCategoryBalance();
            existing.setAccountId(12345678901L);
            existing.setTypeCode("01");
            existing.setCategoryCode(1);
            existing.setBalance(new BigDecimal("500.00"));

            when(categoryBalanceRepository.findByKey(12345678901L, "01", 1))
                    .thenReturn(Optional.of(existing));

            service.updateCategoryBalance(12345678901L, tx);

            verify(categoryBalanceRepository).update(existing);
            assertEquals(new BigDecimal("600.00"), existing.getBalance());
        }
    }

    @Nested
    @DisplayName("processDailyTransaction()")
    class ProcessDailyTransactionTests {

        @Test
        @DisplayName("Should return true for valid transactions and increment count")
        void shouldProcessValidTransaction() {
            Transaction tx = createDailyTransaction("1234567890123456",
                    new BigDecimal("100.00"));
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);
            Account account = createAccount(12345678901L,
                    new BigDecimal("500.00"),
                    new BigDecimal("10000.00"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "2030-12-31");

            when(cardXrefRepository.findByCardNumber("1234567890123456"))
                    .thenReturn(Optional.of(xref));
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(categoryBalanceRepository.findByKey(anyLong(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());

            boolean result = service.processDailyTransaction(tx);

            assertTrue(result);
            assertEquals(1, service.getTransactionCount());
            assertEquals(0, service.getRejectCount());
            assertEquals(0, service.getReturnCode());
        }

        @Test
        @DisplayName("Should return false for invalid transactions and increment reject count")
        void shouldRejectInvalidTransaction() {
            Transaction tx = createDailyTransaction("9999999999999999",
                    new BigDecimal("100.00"));
            when(cardXrefRepository.findByCardNumber("9999999999999999"))
                    .thenReturn(Optional.empty());

            boolean result = service.processDailyTransaction(tx);

            assertFalse(result);
            assertEquals(0, service.getTransactionCount());
            assertEquals(1, service.getRejectCount());
            assertEquals(4, service.getReturnCode());
        }
    }
}
