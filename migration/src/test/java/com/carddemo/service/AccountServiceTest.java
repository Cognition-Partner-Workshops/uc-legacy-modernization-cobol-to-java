package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private AccountService service;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAcctId("00000000001");
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("5000.00"));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));
        account.setGroupId("DEFAULT");
    }

    @Test
    void getAccount_found() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        Account result = service.getAccount("00000000001");
        assertEquals("00000000001", result.getAcctId());
        assertEquals("Y", result.getActiveStatus());
    }

    @Test
    void getAccount_notFound_throwsException() {
        when(accountRepository.findById("99999999999")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.getAccount("99999999999"));
    }

    @Test
    void updateAccount_updatesFields() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");
        request.setCreditLimit(new BigDecimal("20000.00"));
        request.setGroupId("PREMIUM");

        Account result = service.updateAccount("00000000001", request);

        assertEquals("N", result.getActiveStatus());
        assertEquals(new BigDecimal("20000.00"), result.getCreditLimit());
        assertEquals("PREMIUM", result.getGroupId());
    }

    @Test
    void updateAccount_partialUpdate_preservesExisting() {
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");
        // Not setting creditLimit - should preserve existing

        Account result = service.updateAccount("00000000001", request);

        assertEquals("N", result.getActiveStatus());
        assertEquals(new BigDecimal("10000.00"), result.getCreditLimit());
    }
}
