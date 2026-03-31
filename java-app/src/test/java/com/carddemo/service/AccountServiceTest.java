package com.carddemo.service;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .acctId(1L)
                .activeStatus("Y")
                .currBal(new BigDecimal("500.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .openDate("2020-01-01")
                .expirationDate("2030-12-31")
                .reissueDate("2025-01-01")
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .groupId("A000000000")
                .build();
    }

    @Test
    void getAccount_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.getAccount(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAcctId());
    }

    @Test
    void getAccount_notFound_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(999L));
    }

    @Test
    void getAccountDetails_returnsAccountCustomerAndXrefs() {
        CardXref xref = CardXref.builder().cardNum("4000123456789010").custId(1L).acctId(1L).build();
        Customer customer = Customer.builder().custId(1L).firstName("John").lastName("Doe").build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(cardXrefRepository.findByAcctId(1L)).thenReturn(List.of(xref));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Map<String, Object> details = accountService.getAccountDetails(1L);

        assertNotNull(details.get("account"));
        assertNotNull(details.get("customer"));
        assertNotNull(details.get("cardXrefs"));
    }

    @Test
    void updateAccount_updatesFields() {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setAcctId(1L);
        request.setActiveStatus("N");
        request.setCreditLimit(new BigDecimal("10000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account result = accountService.updateAccount(request);

        assertEquals("N", result.getActiveStatus());
        assertEquals(new BigDecimal("10000.00"), result.getCreditLimit());
    }

    @Test
    void updateBalance_credit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        accountService.updateBalance(1L, new BigDecimal("100.00"), true);

        assertEquals(new BigDecimal("600.00"), testAccount.getCurrBal());
        assertEquals(new BigDecimal("100.00"), testAccount.getCurrCycCredit());
    }

    @Test
    void updateBalance_debit() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        accountService.updateBalance(1L, new BigDecimal("-50.00"), false);

        assertEquals(new BigDecimal("450.00"), testAccount.getCurrBal());
        assertEquals(new BigDecimal("50.00"), testAccount.getCurrCycDebit());
    }

    @Test
    void validateAccountId_valid() {
        assertTrue(accountService.validateAccountId(12345678901L));
    }

    @Test
    void validateAccountId_null() {
        assertFalse(accountService.validateAccountId(null));
    }

    @Test
    void validateAccountId_zero() {
        assertFalse(accountService.validateAccountId(0L));
    }
}
