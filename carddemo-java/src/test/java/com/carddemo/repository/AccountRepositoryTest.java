package com.carddemo.repository;

import com.carddemo.entity.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        testAccount = new Account();
        testAccount.setAcctId(100L);
        testAccount.setActiveStatus("Y");
        testAccount.setCurrentBalance(new BigDecimal("5000.00"));
        testAccount.setCreditLimit(new BigDecimal("10000.00"));
        testAccount.setCashCreditLimit(new BigDecimal("3000.00"));
        testAccount.setOpenDate(LocalDate.of(2020, 1, 1));
        testAccount.setExpirationDate(LocalDate.of(2025, 12, 31));
        testAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        testAccount.setGroupId("A000000000");
        testAccount = accountRepository.save(testAccount);
    }

    @Test
    void save_andFindById_returnsAccount() {
        Optional<Account> found = accountRepository.findById(100L);

        assertTrue(found.isPresent());
        assertEquals("Y", found.get().getActiveStatus());
        assertEquals(new BigDecimal("5000.00"), found.get().getCurrentBalance());
    }

    @Test
    void findByActiveStatus_returnsActiveAccounts() {
        Account inactiveAccount = new Account();
        inactiveAccount.setAcctId(101L);
        inactiveAccount.setActiveStatus("N");
        inactiveAccount.setCurrentBalance(BigDecimal.ZERO);
        inactiveAccount.setCreditLimit(BigDecimal.ZERO);
        inactiveAccount.setCashCreditLimit(BigDecimal.ZERO);
        inactiveAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        inactiveAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(inactiveAccount);

        List<Account> activeAccounts = accountRepository.findByActiveStatus("Y");
        List<Account> inactiveAccounts = accountRepository.findByActiveStatus("N");

        assertEquals(1, activeAccounts.size());
        assertEquals(100L, activeAccounts.get(0).getAcctId());
        assertEquals(1, inactiveAccounts.size());
        assertEquals(101L, inactiveAccounts.get(0).getAcctId());
    }

    @Test
    void update_modifiesExistingAccount() {
        testAccount.setCurrentBalance(new BigDecimal("7500.00"));
        accountRepository.save(testAccount);

        Account updated = accountRepository.findById(100L).orElseThrow();
        assertEquals(new BigDecimal("7500.00"), updated.getCurrentBalance());
    }

    @Test
    void delete_removesAccount() {
        accountRepository.deleteById(100L);

        assertFalse(accountRepository.findById(100L).isPresent());
    }

    @Test
    void findAll_returnsAllAccounts() {
        Account account2 = new Account();
        account2.setAcctId(102L);
        account2.setActiveStatus("Y");
        account2.setCurrentBalance(BigDecimal.ZERO);
        account2.setCreditLimit(BigDecimal.ZERO);
        account2.setCashCreditLimit(BigDecimal.ZERO);
        account2.setCurrentCycleCredit(BigDecimal.ZERO);
        account2.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(account2);

        List<Account> all = accountRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}
