package com.carddemo.integration;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountExpiredException;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.OverLimitException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for TransactionService (online transaction add).
 * Verifies COBOL COTRN02C online validation logic against real H2 database.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionOnlineIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Test
    @DisplayName("Add valid transaction succeeds (COTRN02C online)")
    void shouldAddValidTransaction() {
        // Ensure account has future expiration
        Account account = accountRepository.findById("00000000001").orElseThrow();
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(account);

        String validCard = cardXrefRepository.findByAcctId("00000000001").get(0).getCardNum();

        TransactionRequest request = new TransactionRequest();
        request.setCardNum(validCard);
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setDescription("Test purchase");
        request.setSource("Online");

        Transaction result = transactionService.addTransaction(request);

        assertNotNull(result);
        assertNotNull(result.getTranId());
        assertEquals(16, result.getTranId().length(), "Transaction ID should be 16 chars");
        assertEquals("01", result.getTypeCd());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getAmount()));
        assertNotNull(result.getOrigTimestamp());
        assertNotNull(result.getProcTimestamp());
    }

    @Test
    @DisplayName("Add transaction with invalid card throws CardNotFoundException")
    void shouldThrow_whenCardNotFound() {
        TransactionRequest request = new TransactionRequest();
        request.setCardNum("9999999999999999");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("100.00"));

        assertThrows(CardNotFoundException.class,
                () -> transactionService.addTransaction(request));
    }

    @Test
    @DisplayName("Add transaction exceeding credit limit throws OverLimitException")
    void shouldThrow_whenOverLimit() {
        // Account 00000000001 has creditLimit=202000 from data.sql
        // To trigger overlimit: cycleCredit - cycleDebit + amount > creditLimit
        // 201900 - 0 + 200 = 202100 > 202000
        Account account = accountRepository.findById("00000000001").orElseThrow();
        account.setCurrentCycleCredit(new BigDecimal("201900.00"));
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        accountRepository.save(account);

        String validCard = cardXrefRepository.findByAcctId("00000000001").get(0).getCardNum();

        TransactionRequest request = new TransactionRequest();
        request.setCardNum(validCard);
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("200.00")); // 201900 + 200 = 202100 > 202000

        assertThrows(OverLimitException.class,
                () -> transactionService.addTransaction(request));
    }

    @Test
    @DisplayName("Add transaction on expired account throws AccountExpiredException")
    void shouldThrow_whenAccountExpired() {
        Account account = accountRepository.findById("00000000001").orElseThrow();
        account.setExpirationDate(LocalDate.of(2020, 1, 1));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(account);

        String validCard = cardXrefRepository.findByAcctId("00000000001").get(0).getCardNum();

        TransactionRequest request = new TransactionRequest();
        request.setCardNum(validCard);
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("50.00"));

        assertThrows(AccountExpiredException.class,
                () -> transactionService.addTransaction(request));
    }

    @Test
    @DisplayName("Get transaction by ID returns correct record")
    void shouldGetTransactionById() {
        // First add a transaction
        Account account = accountRepository.findById("00000000001").orElseThrow();
        account.setExpirationDate(LocalDate.of(2030, 12, 31));
        accountRepository.save(account);

        String validCard = cardXrefRepository.findByAcctId("00000000001").get(0).getCardNum();

        TransactionRequest request = new TransactionRequest();
        request.setCardNum(validCard);
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("75.00"));
        request.setDescription("Lookup test");

        Transaction created = transactionService.addTransaction(request);

        Transaction found = transactionService.getTransaction(created.getTranId());
        assertEquals(created.getTranId(), found.getTranId());
        assertEquals("Lookup test", found.getDescription());
    }
}
