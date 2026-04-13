package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.CreditCard;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAcctId(1L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("10000.00"));
        testAccount.setCreditLimit(new BigDecimal("20000.00"));
        testAccount.setCashCreditLimit(new BigDecimal("5000.00"));
        testAccount.setOpenDate(LocalDate.of(2020, 1, 1));
        testAccount.setExpirationDate(LocalDate.of(2025, 12, 31));
        testAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        testAccount.setGroupId("A000000000");
    }

    @Test
    void viewAccount_existingAccount_returnsAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        Account result = accountService.viewAccount(1L);

        assertNotNull(result);
        assertEquals(1L, result.getAcctId());
        assertEquals("Y", result.getActiveStatus());
        assertEquals(new BigDecimal("10000.00"), result.getCurrentBalance());
    }

    @Test
    void viewAccount_nonExistentAccount_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> accountService.viewAccount(999L));
    }

    @Test
    void updateAccount_updateBalance_savesUpdatedAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account updates = new Account();
        updates.setCreditLimit(new BigDecimal("25000.00"));
        Account result = accountService.updateAccount(1L, updates);

        assertNotNull(result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_updateStatus_savesUpdatedAccount() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account updates = new Account();
        updates.setActiveStatus("N");
        Account result = accountService.updateAccount(1L, updates);

        assertNotNull(result);
        assertEquals("N", testAccount.getActiveStatus());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void listAccounts_returnsPagedResults() {
        Page<Account> page = new PageImpl<>(List.of(testAccount));
        when(accountRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<Account> result = accountService.listAccounts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testAccount, result.getContent().get(0));
    }

    @Test
    void updateBalance_addsToCurrentBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.updateBalance(1L, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("10500.00"), testAccount.getCurrentBalance());
        verify(accountRepository).save(testAccount);
    }

    @Test
    void getCardsForAccount_returnsCards() {
        CreditCard card = new CreditCard();
        card.setCardNum("1234567890123456");
        card.setAcctId(1L);
        when(creditCardRepository.findByAcctId(1L)).thenReturn(List.of(card));

        List<CreditCard> result = accountService.getCardsForAccount(1L);

        assertEquals(1, result.size());
        assertEquals("1234567890123456", result.get(0).getCardNum());
    }
}
