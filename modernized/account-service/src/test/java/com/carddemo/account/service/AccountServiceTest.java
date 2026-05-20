package com.carddemo.account.service;

import com.carddemo.account.dto.BalanceAdjustmentRequest;
import com.carddemo.account.dto.UpdateAccountRequest;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    void getAccount_found_returnsDto() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .currBal(new BigDecimal("1500.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        var result = accountService.getAccount("00000000001");

        assertThat(result.getAcctId()).isEqualTo("00000000001");
        assertThat(result.getCurrBal()).isEqualByComparingTo("1500.00");
    }

    @Test
    void getAccount_notFound_throws() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount("99999999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAccount_validData_succeeds() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .activeStatus('N')
                .creditLimit(new BigDecimal("6000.00"))
                .version(0L)
                .build();
        var result = accountService.updateAccount("00000000001", request);

        assertThat(result.getActiveStatus()).isEqualTo('N');
        assertThat(result.getCreditLimit()).isEqualByComparingTo("6000.00");
    }

    @Test
    void updateAccount_invalidStatus_throws() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .activeStatus('X')
                .version(0L)
                .build();

        assertThatThrownBy(() -> accountService.updateAccount("00000000001", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("activeStatus");
    }

    @Test
    void updateAccount_negativeCreditLimit_throws() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .creditLimit(new BigDecimal("-100.00"))
                .version(0L)
                .build();

        assertThatThrownBy(() -> accountService.updateAccount("00000000001", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("creditLimit");
    }

    @Test
    void updateAccount_cashCreditLimitExceedsCreditLimit_throws() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .cashCreditLimit(new BigDecimal("6000.00"))
                .version(0L)
                .build();

        assertThatThrownBy(() -> accountService.updateAccount("00000000001", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("cashCreditLimit");
    }

    @Test
    void updateAccount_invalidDateFormat_throws() {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .openDate("01/15/2024")
                .version(0L)
                .build();

        assertThatThrownBy(() -> accountService.updateAccount("00000000001", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("YYYY-MM-DD");
    }

    @Test
    void adjustBalance_positiveAmount() {
        Account account = Account.builder()
                .acctId("00000000001")
                .currBal(new BigDecimal("1000.00"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BalanceAdjustmentRequest request = BalanceAdjustmentRequest.builder()
                .amount(new BigDecimal("250.50"))
                .description("Payment")
                .build();
        var result = accountService.adjustBalance("00000000001", request);

        assertThat(result.getCurrBal()).isEqualByComparingTo("1250.50");
    }

    @Test
    void adjustBalance_negativeAmount() {
        Account account = Account.builder()
                .acctId("00000000001")
                .currBal(new BigDecimal("1000.00"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BalanceAdjustmentRequest request = BalanceAdjustmentRequest.builder()
                .amount(new BigDecimal("-500.00"))
                .description("Refund")
                .build();
        var result = accountService.adjustBalance("00000000001", request);

        assertThat(result.getCurrBal()).isEqualByComparingTo("500.00");
    }

    @Test
    void adjustBalance_bigDecimalPrecision() {
        Account account = Account.builder()
                .acctId("00000000001")
                .currBal(new BigDecimal("999999999.99"))
                .version(0L)
                .build();
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        BalanceAdjustmentRequest request = BalanceAdjustmentRequest.builder()
                .amount(new BigDecimal("0.01"))
                .build();
        var result = accountService.adjustBalance("00000000001", request);

        assertThat(result.getCurrBal()).isEqualByComparingTo("1000000000.00");
    }
}
