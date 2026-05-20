package com.carddemo.account.service;

import com.carddemo.account.dto.BalanceAdjustmentRequest;
import com.carddemo.account.dto.UpdateAccountRequest;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.account.exception.OptimisticLockException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TIER 2 — HIGH RISK: Account CRUD & Data Integrity
 * Risk factors: optimistic locking failure, balance corruption, validation bypass
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-2")
@DisplayName("Tier 2 (High): Account CRUD & Data Integrity")
class RiskBasedAccountTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    // --- Optimistic Locking (stale data protection) ---

    @Test
    @DisplayName("T2-ACCT-001: Update with matching version succeeds")
    void updateAccount_matchingVersion_succeeds() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .activeStatus('N').version(0L).build();
        var result = accountService.updateAccount("00000000001", request);

        assertThat(result.getActiveStatus()).isEqualTo('N');
    }

    @Test
    @DisplayName("T2-ACCT-002: Update with stale version is rejected")
    void updateAccount_staleVersion_throws() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y')
                .version(5L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .activeStatus('N').version(3L).build();

        assertThatThrownBy(() -> accountService.updateAccount("00000000001", request))
                .isInstanceOf(OptimisticLockException.class)
                .hasMessageContaining("modified");
    }

    // --- Business Rule Validation ---

    @Test
    @DisplayName("T2-ACCT-003: Active status 'Y' is accepted")
    void updateAccount_statusY_accepted() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('N').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().activeStatus('Y').version(0L).build());
        assertThat(result.getActiveStatus()).isEqualTo('Y');
    }

    @Test
    @DisplayName("T2-ACCT-004: Active status 'N' is accepted")
    void updateAccount_statusN_accepted() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().activeStatus('N').version(0L).build());
        assertThat(result.getActiveStatus()).isEqualTo('N');
    }

    @Test
    @DisplayName("T2-ACCT-005: Active status 'X' is rejected")
    void updateAccount_statusInvalid_rejected() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().activeStatus('X').version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("activeStatus");
    }

    @Test
    @DisplayName("T2-ACCT-006: Negative credit limit is rejected")
    void updateAccount_negativeCreditLimit_rejected() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().creditLimit(new BigDecimal("-100")).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("creditLimit");
    }

    @Test
    @DisplayName("T2-ACCT-007: Cash credit limit exceeding credit limit is rejected")
    void updateAccount_cashCreditExceedsCreditLimit_rejected() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00")).version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().cashCreditLimit(new BigDecimal("6000.00")).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cashCreditLimit");
    }

    @Test
    @DisplayName("T2-ACCT-008: Invalid date format is rejected (must be YYYY-MM-DD)")
    void updateAccount_invalidDateFormat_rejected() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().openDate("12/31/2024").version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("YYYY-MM-DD");
    }

    @Test
    @DisplayName("T2-ACCT-009: Valid date format YYYY-MM-DD is accepted")
    void updateAccount_validDateFormat_accepted() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y').version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.updateAccount("00000000001",
                UpdateAccountRequest.builder().openDate("2024-12-31").version(0L).build());
        assertThat(result.getOpenDate()).isEqualTo("2024-12-31");
    }

    // --- Balance Adjustment (financial integrity) ---

    @Test
    @DisplayName("T2-ACCT-010: Balance adjustment with positive amount adds correctly")
    void adjustBalance_positiveAmount_addsToBalance() {
        Account account = Account.builder()
                .acctId("00000000001").currBal(new BigDecimal("1000.00")).version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.adjustBalance("00000000001",
                BalanceAdjustmentRequest.builder().amount(new BigDecimal("250.50")).build());
        assertThat(result.getCurrBal()).isEqualByComparingTo("1250.50");
    }

    @Test
    @DisplayName("T2-ACCT-011: Balance adjustment with negative amount subtracts correctly")
    void adjustBalance_negativeAmount_subtractsFromBalance() {
        Account account = Account.builder()
                .acctId("00000000001").currBal(new BigDecimal("1000.00")).version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.adjustBalance("00000000001",
                BalanceAdjustmentRequest.builder().amount(new BigDecimal("-500.00")).build());
        assertThat(result.getCurrBal()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("T2-ACCT-012: Balance adjustment preserves BigDecimal precision at limits")
    void adjustBalance_preservesPrecisionAtLimits() {
        Account account = Account.builder()
                .acctId("00000000001").currBal(new BigDecimal("999999999.99")).version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = accountService.adjustBalance("00000000001",
                BalanceAdjustmentRequest.builder().amount(new BigDecimal("0.01")).build());
        assertThat(result.getCurrBal()).isEqualByComparingTo("1000000000.00");
    }

    // --- Account Lookup ---

    @Test
    @DisplayName("T2-ACCT-013: Get nonexistent account throws ResourceNotFoundException")
    void getAccount_notFound_throws() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("99999999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("T2-ACCT-014: Get account returns all fields correctly")
    void getAccount_returnsAllFields() {
        Account account = Account.builder()
                .acctId("00000000001").activeStatus('Y')
                .currBal(new BigDecimal("1500.75"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .openDate("2020-01-15")
                .expirationDate("2026-01-15")
                .addrZip("10001")
                .groupId("GRP001")
                .currCycCredit(new BigDecimal("200.00"))
                .currCycDebit(new BigDecimal("350.50"))
                .version(0L).build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        var result = accountService.getAccount("00000000001");

        assertThat(result.getAcctId()).isEqualTo("00000000001");
        assertThat(result.getActiveStatus()).isEqualTo('Y');
        assertThat(result.getCurrBal()).isEqualByComparingTo("1500.75");
        assertThat(result.getCreditLimit()).isEqualByComparingTo("5000.00");
        assertThat(result.getCashCreditLimit()).isEqualByComparingTo("1000.00");
        assertThat(result.getOpenDate()).isEqualTo("2020-01-15");
        assertThat(result.getExpirationDate()).isEqualTo("2026-01-15");
        assertThat(result.getAddrZip()).isEqualTo("10001");
        assertThat(result.getGroupId()).isEqualTo("GRP001");
    }
}
