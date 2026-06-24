package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.DisclosureGroup;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DisclosureGroupRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InterestCalculationService (COBOL CBACT04C equivalent).
 */
@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;
    @Mock
    private DisclosureGroupRepository disclosureGroupRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private InterestCalculationService service;

    @BeforeEach
    void setUp() {
        service = new InterestCalculationService(
                accountRepository, cardXrefRepository,
                disclosureGroupRepository, transactionRepository);
    }

    @Nested
    @DisplayName("computeMonthlyInterest()")
    class ComputeMonthlyInterestTests {

        @Test
        @DisplayName("Should compute correct monthly interest")
        void shouldComputeCorrectMonthlyInterest() {
            // (1000 * 18) / 1200 = 15.00
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), new BigDecimal("18.00"));

            assertEquals(new BigDecimal("15.00"), result);
        }

        @Test
        @DisplayName("Should return zero for zero balance")
        void shouldReturnZeroForZeroBalance() {
            BigDecimal result = service.computeMonthlyInterest(
                    BigDecimal.ZERO, new BigDecimal("18.00"));

            assertEquals(0, result.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should return zero for zero interest rate")
        void shouldReturnZeroForZeroRate() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), BigDecimal.ZERO);

            assertEquals(0, result.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should return zero for null balance")
        void shouldReturnZeroForNullBalance() {
            BigDecimal result = service.computeMonthlyInterest(null, new BigDecimal("18.00"));

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should return zero for null rate")
        void shouldReturnZeroForNullRate() {
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("1000.00"), null);

            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Should handle negative balance (credit)")
        void shouldHandleNegativeBalance() {
            // (-500 * 18) / 1200 = -7.50
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("-500.00"), new BigDecimal("18.00"));

            assertEquals(new BigDecimal("-7.50"), result);
        }

        @Test
        @DisplayName("Should handle large balance with rounding")
        void shouldHandleLargeBalanceWithRounding() {
            // (99999999.99 * 24.99) / 1200 = 2083312.50 (rounded)
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("99999999.99"), new BigDecimal("24.99"));

            assertNotNull(result);
            assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Should handle fractional interest rate")
        void shouldHandleFractionalInterestRate() {
            // (10000 * 5.75) / 1200 = 47.92
            BigDecimal result = service.computeMonthlyInterest(
                    new BigDecimal("10000.00"), new BigDecimal("5.75"));

            assertEquals(new BigDecimal("47.92"), result);
        }
    }

    @Nested
    @DisplayName("getInterestRate()")
    class GetInterestRateTests {

        @Test
        @DisplayName("Should return account-specific interest rate when found")
        void shouldReturnAccountSpecificRate() {
            DisclosureGroup discGroup = new DisclosureGroup();
            discGroup.setInterestRate(new BigDecimal("18.00"));
            when(disclosureGroupRepository.findByKey("GROUP1", "01", 5))
                    .thenReturn(Optional.of(discGroup));

            BigDecimal rate = service.getInterestRate("GROUP1", "01", 5);

            assertEquals(new BigDecimal("18.00"), rate);
        }

        @Test
        @DisplayName("Should fallback to DEFAULT group when account group not found")
        void shouldFallbackToDefaultGroup() {
            when(disclosureGroupRepository.findByKey("GROUP1", "01", 5))
                    .thenReturn(Optional.empty());
            DisclosureGroup defaultGroup = new DisclosureGroup();
            defaultGroup.setInterestRate(new BigDecimal("12.00"));
            when(disclosureGroupRepository.findByKey("DEFAULT", "01", 5))
                    .thenReturn(Optional.of(defaultGroup));

            BigDecimal rate = service.getInterestRate("GROUP1", "01", 5);

            assertEquals(new BigDecimal("12.00"), rate);
        }

        @Test
        @DisplayName("Should return zero when no group found at all")
        void shouldReturnZeroWhenNoGroupFound() {
            when(disclosureGroupRepository.findByKey("GROUP1", "01", 5))
                    .thenReturn(Optional.empty());
            when(disclosureGroupRepository.findByKey("DEFAULT", "01", 5))
                    .thenReturn(Optional.empty());

            BigDecimal rate = service.getInterestRate("GROUP1", "01", 5);

            assertEquals(BigDecimal.ZERO, rate);
        }
    }

    @Nested
    @DisplayName("processAccountInterest()")
    class ProcessAccountInterestTests {

        private Account createTestAccount() {
            Account account = new Account();
            account.setAccountId(12345678901L);
            account.setCurrentBalance(new BigDecimal("5000.00"));
            account.setCreditLimit(new BigDecimal("10000.00"));
            account.setCurrentCycleCredit(new BigDecimal("1000.00"));
            account.setCurrentCycleDebit(new BigDecimal("-200.00"));
            account.setGroupId("GROUP1");
            return account;
        }

        @Test
        @DisplayName("Should process interest for multiple category balances")
        void shouldProcessInterestForMultipleBalances() {
            Account account = createTestAccount();
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));

            DisclosureGroup discGroup = new DisclosureGroup();
            discGroup.setInterestRate(new BigDecimal("18.00"));
            when(disclosureGroupRepository.findByKey(eq("GROUP1"), anyString(), anyInt()))
                    .thenReturn(Optional.of(discGroup));

            TransactionCategoryBalance catBal1 = new TransactionCategoryBalance();
            catBal1.setAccountId(12345678901L);
            catBal1.setTypeCode("01");
            catBal1.setCategoryCode(1);
            catBal1.setBalance(new BigDecimal("1000.00"));

            TransactionCategoryBalance catBal2 = new TransactionCategoryBalance();
            catBal2.setAccountId(12345678901L);
            catBal2.setTypeCode("02");
            catBal2.setCategoryCode(2);
            catBal2.setBalance(new BigDecimal("2000.00"));

            List<TransactionCategoryBalance> balances = Arrays.asList(catBal1, catBal2);

            BigDecimal totalInterest = service.processAccountInterest(
                    12345678901L, balances, "2025-01-15");

            // Interest: (1000*18)/1200 + (2000*18)/1200 = 15 + 30 = 45
            assertEquals(new BigDecimal("45.00"), totalInterest);

            // Verify two interest transactions were written
            verify(transactionRepository, times(2)).save(any(Transaction.class));

            // Verify account was updated
            verify(accountRepository).update(account);
            // Balance should be 5000 + 45 = 5045
            assertEquals(new BigDecimal("5045.00"), account.getCurrentBalance());
            // Cycle balances should be reset
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleDebit());
        }

        @Test
        @DisplayName("Should skip categories with zero interest rate")
        void shouldSkipZeroInterestRate() {
            Account account = createTestAccount();
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));

            // Return zero interest rate
            when(disclosureGroupRepository.findByKey(anyString(), anyString(), anyInt()))
                    .thenReturn(Optional.empty());

            TransactionCategoryBalance catBal = new TransactionCategoryBalance();
            catBal.setAccountId(12345678901L);
            catBal.setTypeCode("01");
            catBal.setCategoryCode(1);
            catBal.setBalance(new BigDecimal("1000.00"));

            BigDecimal totalInterest = service.processAccountInterest(
                    12345678901L, Collections.singletonList(catBal), "2025-01-15");

            assertEquals(0, totalInterest.compareTo(BigDecimal.ZERO));
            verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should handle empty category balance list")
        void shouldHandleEmptyBalanceList() {
            Account account = createTestAccount();
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(xref));

            BigDecimal totalInterest = service.processAccountInterest(
                    12345678901L, Collections.emptyList(), "2025-01-15");

            assertEquals(0, totalInterest.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should throw when account not found")
        void shouldThrowWhenAccountNotFound() {
            when(accountRepository.findByAccountId(99999999999L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> service.processAccountInterest(
                            99999999999L, Collections.emptyList(), "2025-01-15"));
        }

        @Test
        @DisplayName("Should throw when card xref not found")
        void shouldThrowWhenXrefNotFound() {
            Account account = createTestAccount();
            when(accountRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.of(account));
            when(cardXrefRepository.findByAccountId(12345678901L))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class,
                    () -> service.processAccountInterest(
                            12345678901L, Collections.emptyList(), "2025-01-15"));
        }
    }

    @Nested
    @DisplayName("writeInterestTransaction()")
    class WriteInterestTransactionTests {

        @Test
        @DisplayName("Should write interest transaction with correct fields")
        void shouldWriteInterestTransactionWithCorrectFields() {
            Account account = new Account();
            account.setAccountId(12345678901L);
            CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

            service.writeInterestTransaction(account, xref,
                    new BigDecimal("15.00"), "2025-01-15");

            ArgumentCaptor<Transaction> captor =
                    ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());

            Transaction saved = captor.getValue();
            assertTrue(saved.getTransactionId().startsWith("2025-01-15"));
            assertEquals("01", saved.getTypeCode());
            assertEquals(5, saved.getCategoryCode());
            assertEquals("System", saved.getSource());
            assertTrue(saved.getDescription().contains("Int. for a/c"));
            assertEquals(new BigDecimal("15.00"), saved.getAmount());
            assertEquals("1234567890123456", saved.getCardNumber());
            assertEquals(0, saved.getMerchantId());
            assertNotNull(saved.getOriginTimestamp());
            assertNotNull(saved.getProcessedTimestamp());
        }
    }

    @Nested
    @DisplayName("updateAccountWithInterest()")
    class UpdateAccountWithInterestTests {

        @Test
        @DisplayName("Should add interest and reset cycle balances")
        void shouldAddInterestAndResetCycleBalances() {
            Account account = new Account();
            account.setAccountId(12345678901L);
            account.setCurrentBalance(new BigDecimal("5000.00"));
            account.setCurrentCycleCredit(new BigDecimal("1000.00"));
            account.setCurrentCycleDebit(new BigDecimal("-200.00"));

            service.updateAccountWithInterest(account, new BigDecimal("45.00"));

            assertEquals(new BigDecimal("5045.00"), account.getCurrentBalance());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleDebit());
            verify(accountRepository).update(account);
        }

        @Test
        @DisplayName("Should handle zero interest")
        void shouldHandleZeroInterest() {
            Account account = new Account();
            account.setAccountId(12345678901L);
            account.setCurrentBalance(new BigDecimal("5000.00"));
            account.setCurrentCycleCredit(new BigDecimal("1000.00"));
            account.setCurrentCycleDebit(new BigDecimal("-200.00"));

            service.updateAccountWithInterest(account, BigDecimal.ZERO);

            assertEquals(new BigDecimal("5000.00"), account.getCurrentBalance());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
            assertEquals(BigDecimal.ZERO, account.getCurrentCycleDebit());
        }
    }

    @Test
    @DisplayName("Should track record count correctly")
    void shouldTrackRecordCount() {
        assertEquals(0, service.getRecordCount());

        Account account = new Account();
        account.setAccountId(12345678901L);
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setGroupId("GROUP1");
        CardXref xref = new CardXref("1234567890123456", 1001L, 12345678901L);

        when(accountRepository.findByAccountId(12345678901L))
                .thenReturn(Optional.of(account));
        when(cardXrefRepository.findByAccountId(12345678901L))
                .thenReturn(Optional.of(xref));
        when(disclosureGroupRepository.findByKey(anyString(), anyString(), anyInt()))
                .thenReturn(Optional.empty());

        TransactionCategoryBalance catBal = new TransactionCategoryBalance();
        catBal.setTypeCode("01");
        catBal.setCategoryCode(1);
        catBal.setBalance(new BigDecimal("100.00"));

        service.processAccountInterest(12345678901L,
                Collections.singletonList(catBal), "2025-01-15");

        assertEquals(1, service.getRecordCount());
    }
}
